/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.alfresco.mobile.android.api.utils.IOUtils;
import org.alfresco.mobile.android.application.exception.AlfrescoAppException;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.sync.SyncOperation;
import org.alfresco.mobile.android.application.operations.sync.SynchroProvider;
import org.alfresco.mobile.android.application.operations.sync.SynchroSchema;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class EncryptionUtils
{
    private static final String TAG = EncryptionUtils.class.getName();

    private static final byte[] SALT = { 0x0A, 0x02, 0x13, 0x3C, 0x3B, 0x0F, 0x1A };

    private static final int COUNT = 10;

    private static final byte[] REFERENCE_DATA = "AlfrescoCrypto".getBytes(Charset.forName("UTF-8"));

    private static final String ALGORITHM = "PBEWithMD5AndDES";

    private static final int KEY_LENGTH = 128;

    private static final String INFO_FILE = "tmp.qzv";

    private static SecretKey info = null;

    private static final int MAX_BUFFER_SIZE = 10240;

    private static ArrayList<String> filesDecrypted = null;

    private static final String DECRYPTION_EXTENSION = ".utmp";

    private static ArrayList<String> filesEncrypted = null;

    private static final String ENCRYPTION_EXTENSION = ".etmp";

    private EncryptionUtils()
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DETECTION
    // ///////////////////////////////////////////////////////////////////////////
    public static boolean isEncrypted(String filename)
    {
        return (filename.endsWith(DECRYPTION_EXTENSION) || filename.endsWith(ENCRYPTION_EXTENSION));
    }

    public static boolean isEncrypted(Context ctxt, String filename) throws IOException
    {
        File source = new File(filename);
        InputStream sourceFile = null;
        try
        {
            sourceFile = testInputStream(new FileInputStream(source), generateKey(ctxt, KEY_LENGTH).toString());
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.d(TAG, Log.getStackTraceString(e));
        }

        if (sourceFile != null)
        {
            sourceFile.close();
            return true;
        }

        return false;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DECRYPTION
    // ///////////////////////////////////////////////////////////////////////////
    public static boolean decryptFile(Context ctxt, String filename) throws AlfrescoAppException
    {
        return decryptFile(ctxt, filename, null);
    }

    public static boolean decryptFile(Context ctxt, String filename, String newFilename) throws AlfrescoAppException
    {
        boolean ret = true;
        OutputStream destStream = null;
        InputStream sourceStream = null;
        try
        {
            if (isEncrypted(ctxt, filename))
            {
                File source = new File(filename);
                long size = source.length();
                long copied = 0;
                File dest = new File(newFilename != null ? newFilename : filename + ".utmp");
                sourceStream = wrapCipherInputStream(new FileInputStream(source), generateKey(ctxt, KEY_LENGTH)
                        .toString());
                destStream = new FileOutputStream(dest);
                int nBytes = 0;

                byte[] buffer = new byte[MAX_BUFFER_SIZE];

                while (size - copied > 0)
                {
                    if (size - copied < MAX_BUFFER_SIZE)
                    {
                        buffer = new byte[(int) (size - copied)];
                    }
                    nBytes = sourceStream.read(buffer);
                    if (nBytes == -1)
                    {
                        break;
                    }
                    else if (nBytes > 0)
                    {
                        destStream.write(buffer);
                    }
                }

                sourceStream.close();
                destStream.close();

                if (newFilename == null)
                {
                    if (source.delete())
                    {
                        // Rename decrypted file to original name.
                        if (!(ret = dest.renameTo(source)))
                        {
                            Log.e(TAG, "Cannot rename decrypted file " + dest.getName());
                        }
                    }
                    else
                    {
                        Log.e(TAG, "Cannot delete original file " + source.getName());
                        dest.delete();
                        ret = false;
                    }
                }
            }
            else
            {
                Log.w(TAG, "File is already decrypted: " + filename);
                return true;
            }
        }
        catch (Exception e)
        {
            IOUtils.closeStream(sourceStream);
            IOUtils.closeStream(destStream);
            throw new AlfrescoAppException(-1, e);
        }
        return ret;
    }

    /*
     * Encrypt an entire folder, recursively if required. Rollback is
     * implemented if any failures occur. NOTE: This method is not thread-safe.
     */
    public static boolean decryptFiles(Context ctxt, String sourceFolder, boolean recursive)
    {
        boolean startPoint = false;
        boolean result = true;

        if (filesDecrypted == null)
        {
            filesDecrypted = new ArrayList<String>();
            startPoint = true;
        }
        try
        {
            File f = new File(sourceFolder);
            File file[] = f.listFiles();

            for (int i = 0; i < file.length; i++)
            {
                File sourceFile = file[i];
                String destFilename = file[i].getPath() + DECRYPTION_EXTENSION;

                if (!sourceFile.isHidden())
                {
                    if (sourceFile.isFile())
                    {
                        result = decryptFile(ctxt, sourceFile.getPath(), destFilename);
                        if (result)
                        {
                            filesDecrypted.add(sourceFile.getPath());
                        }
                    }
                    else
                    {
                        if (sourceFile.isDirectory() && recursive && !sourceFile.getName().equals(".")
                                && !sourceFile.getName().equals(".."))
                        {
                            result = decryptFiles(ctxt, sourceFile.getPath(), recursive);
                        }
                    }

                    if (!result)
                    {
                        if (filesDecrypted != null)
                        {
                            Log.e(TAG, "Folder decryption failed for " + sourceFile.getName());

                            // Remove the decrypted versions done so far.
                            Log.d(TAG, "Decryption rollback in progress...");
                            for (int j = 0; j < filesDecrypted.size(); j++)
                            {
                                if (new File(filesDecrypted.get(j) + DECRYPTION_EXTENSION).delete())
                                {
                                    Log.w(TAG, "Deleted decrypted version of " + filesDecrypted.get(j));
                                }
                            }
                            filesDecrypted.clear();
                            filesDecrypted = null;
                        }

                        break;
                    }
                }
            }

            if (result && startPoint)
            {
                // Whole folder decrypt succeeded. Move over to new decrypted
                // versions.
                File src = null, dest = null, tempSrc = null;
                Uri uri = null;
                Cursor favoriteCursor = null;
                ContentValues cValues = null;
                int statut = 0;

                for (int j = 0; j < filesDecrypted.size(); j++)
                {
                    src = new File(filesDecrypted.get(j));
                    dest = new File(filesDecrypted.get(j) + DECRYPTION_EXTENSION);

                    //
                    // Two-stage delete for failsafe operation.
                    //
                    tempSrc = new File(filesDecrypted.get(j) + ".mov");
                    if (src.renameTo(tempSrc))
                    {
                        // Put decrypted version in originals place.
                        if (dest.renameTo(src))
                        {
                            // Delete the original decrypted temp file.
                            if (!tempSrc.delete())
                            {
                                Log.w(TAG, "Could not delete original file " + tempSrc.getPath());
                            }

                            // If the file lives in Sync folder
                            if (StorageManager.isSynchroFile(ctxt, src))
                            {
                                try
                                {
                                    favoriteCursor = ctxt.getContentResolver().query(
                                            SynchroProvider.CONTENT_URI,
                                            SynchroSchema.COLUMN_ALL,
                                            SynchroSchema.COLUMN_LOCAL_URI + " LIKE '" + Uri.fromFile(src).toString()
                                                    + "%'", null, null);

                                    if (favoriteCursor.getCount() == 1 && favoriteCursor.moveToFirst())
                                    {
                                        statut = favoriteCursor.getInt(SynchroSchema.COLUMN_STATUS_ID);
                                        if (statut != SyncOperation.STATUS_MODIFIED)
                                        {
                                            uri = Uri.parse(SynchroProvider.CONTENT_URI + "/"
                                                    + favoriteCursor.getLong(SynchroSchema.COLUMN_ID_ID));
                                            if (cValues == null)
                                            {
                                                cValues = new ContentValues();
                                            }
                                            cValues.put(SynchroSchema.COLUMN_LOCAL_MODIFICATION_TIMESTAMP,
                                                    src.lastModified());
                                            ctxt.getContentResolver().update(uri, cValues, null, null);
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                                finally
                                {
                                    if (favoriteCursor != null)
                                    {
                                        favoriteCursor.close();
                                    }
                                }
                            }
                        }
                        else
                        {
                            tempSrc.renameTo(src);
                        }
                    }
                }
                filesDecrypted.clear();
                filesDecrypted = null;
            }

            return result;
        }
        catch (Exception e)
        {
            Log.d(TAG, Log.getStackTraceString(e));
            return false;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ENCRYPTION
    // ///////////////////////////////////////////////////////////////////////////
    /*
     * Encrypt file in place, leaving original file unencrypted.
     */
    public static boolean encryptFile(Context ctxt, String filename, boolean nuke) throws AlfrescoAppException
    {
        return encryptFile(ctxt, filename, null, nuke);
    }

    /*
     * Encrypt file in place, leaving no trace of original unencrypted data.
     * filename file to encrypt nuke whether to zero the original unencrypted
     * file before attempting its deletion, for additional security.
     */
    public static boolean encryptFile(Context ctxt, String filename, String newFilename, boolean nuke)
            throws AlfrescoAppException
    {
        boolean ret = true;
        OutputStream destStream = null;
        InputStream sourceStream = null;
        try
        {

            if (!isEncrypted(ctxt, filename))
            {
                File source = new File(filename);
                long size = source.length();
                long copied = 0;
                File dest = new File(newFilename != null ? newFilename : filename + ".etmp");
                sourceStream = new FileInputStream(source);
                destStream = wrapCipherOutputStream(new FileOutputStream(dest), generateKey(ctxt, KEY_LENGTH)
                        .toString());
                int nBytes = 0;
                byte buffer[] = new byte[MAX_BUFFER_SIZE];

                Log.i(TAG, "Encrypting file " + filename);

                while (size - copied > 0)
                {
                    if (size - copied < MAX_BUFFER_SIZE)
                    {
                        buffer = new byte[(int) (size - copied)];
                    }
                    nBytes = sourceStream.read(buffer);
                    if (nBytes == -1)
                    {
                        break;
                    }
                    else if (nBytes > 0)
                    {
                        destStream.write(buffer);
                    }
                }

                sourceStream.close();
                destStream.flush();
                destStream.close();

                if (newFilename == null)
                {
                    if (nuke)
                    {
                        nukeFile(source, size);
                    }

                    if (source.delete())
                    {
                        // Rename encrypted file to original name.
                        if (!(ret = dest.renameTo(source)))
                        {
                            Log.e(TAG, "Cannot rename encrypted file " + dest.getName());
                        }
                    }
                    else
                    {
                        Log.e(TAG, "Cannot delete original file " + source.getName());

                        dest.delete();
                        ret = false;
                    }
                }

                return ret;
            }
            else
            {
                Log.w(TAG, "File is already encrypted: " + filename);
                return true;
            }
        }
        catch (Exception e)
        {
            IOUtils.closeStream(sourceStream);
            IOUtils.closeStream(destStream);
            throw new AlfrescoAppException(-1, e);
        }
    }

    /*
     * Encrypt an entire folder, recursively if required. Rollback is
     * implemented if any failures occur. NOTE: This method is not thread-safe.
     */
    public static boolean encryptFiles(Context ctxt, String sourceFolder, boolean recursive)
    {
        boolean startPoint = false;
        boolean result = true;

        if (filesEncrypted == null)
        {
            filesEncrypted = new ArrayList<String>();
            startPoint = true;
        }
        try
        {
            File f = new File(sourceFolder);
            File file[] = f.listFiles();

            for (int i = 0; i < file.length; i++)
            {
                File sourceFile = file[i];
                String destFilename = file[i].getPath() + ENCRYPTION_EXTENSION;

                if (!sourceFile.isHidden())
                {
                    if (sourceFile.isFile())
                    {
                        result = encryptFile(ctxt, sourceFile.getPath(), destFilename, true);
                        if (result)
                        {
                            filesEncrypted.add(sourceFile.getPath());
                        }
                    }
                    else
                    {
                        if (sourceFile.isDirectory() && recursive && !sourceFile.getName().equals(".")
                                && !sourceFile.getName().equals(".."))
                        {
                            result = encryptFiles(ctxt, sourceFile.getPath(), recursive);
                        }
                    }

                    if (!result)
                    {
                        if (filesEncrypted != null)
                        {
                            Log.e(TAG, "Folder encryption failed for " + sourceFile.getName());

                            // Remove the encrypted versions done so far.
                            Log.i(TAG, "Encryption rollback in progress...");
                            for (int j = 0; j < filesEncrypted.size(); j++)
                            {
                                if (new File(filesEncrypted.get(j) + ENCRYPTION_EXTENSION).delete())
                                {
                                    Log.i(TAG, "Deleted encrypted version of " + filesEncrypted.get(j));
                                }
                            }
                            filesEncrypted.clear();
                            filesEncrypted = null;
                        }

                        break;
                    }
                }
            }

            if (result && startPoint)
            {
                // Whole folder encrypt succeeded. Move over to new encrypted
                // versions.

                for (int j = 0; j < filesEncrypted.size(); j++)
                {
                    File src = new File(filesEncrypted.get(j));
                    File dest = new File(filesEncrypted.get(j) + ENCRYPTION_EXTENSION);

                    //
                    // Two-stage delete for failsafe operation.
                    //
                    File tempSrc = new File(filesEncrypted.get(j) + ".mov");
                    if (src.renameTo(tempSrc))
                    {
                        // Put encrypted version in originals place.
                        if (dest.renameTo(src))
                        {
                            // Delete the original unencrypted temp file.
                            if (!tempSrc.delete())
                            {
                                // At least rename it out of the way with a temp
                                // extension, and nuke its content.
                                Log.w(TAG,
                                        "Could not delete original file. Nuking and renaming it " + tempSrc.getPath());
                                nukeFile(tempSrc, -1);
                            }
                        }
                        else
                        {
                            tempSrc.renameTo(src);
                        }
                    }
                }
                filesEncrypted.clear();
                filesEncrypted = null;
            }

            return result;
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error during folder encryption: " + e.getMessage());
            Log.d(TAG, Log.getStackTraceString(e));

            return false;
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private static InputStream testInputStream(InputStream streamIn, String password)
    {
        try
        {
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, COUNT);

            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

            Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
            pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

            int count = streamIn.read();
            if (count <= 0 || count > 1024) { return null; }

            byte[] input = new byte[count];
            streamIn.read(input);
            pbeCipher.doFinal(input).toString().contains(Arrays.toString(REFERENCE_DATA));

            return new CipherInputStream(streamIn, pbeCipher);
        }
        catch (IOException io)
        {

        }
        catch (GeneralSecurityException ge)
        {

        }

        return null;
    }

    public static OutputStream wrapCipherOutputStream(OutputStream streamOut, String password) throws IOException,
            GeneralSecurityException
    {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, COUNT);

        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
        pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

        /*
         * Write predefined data first (see the reading code)
         */
        byte[] output = pbeCipher.doFinal(REFERENCE_DATA);
        streamOut.write(output.length);
        streamOut.write(output);

        return new CipherOutputStream(streamOut, pbeCipher);
    }

    private static InputStream wrapCipherInputStream(InputStream streamIn, String password) throws IOException,
            GeneralSecurityException
    {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, COUNT);

        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
        pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

        /*
         * Read a predefined data block. If the password is incorrect, we'll get
         * a security exception here. Without this, we will only get an
         * IOException later when reading the CipherInputStream, which is not
         * specific enough for a good error message.
         */
        int count = streamIn.read();
        if (count <= 0 || count > 1024) { throw new IOException("Bad encrypted file"); }

        byte[] input = new byte[count];
        streamIn.read(input);
        pbeCipher.doFinal(input);

        return new CipherInputStream(streamIn, pbeCipher);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // KEY GENERATOR
    // ///////////////////////////////////////////////////////////////////////////
    private static SecretKey generateKey(Context ctxt, int bits) throws IOException, NoSuchAlgorithmException
    {
        if (info != null) { return info; }

        SecretKey r;
        FileInputStream fis = null;
        try
        {
            fis = ctxt.openFileInput(INFO_FILE);
        }
        catch (FileNotFoundException e)
        {
            fis = null;
        }

        if (fis != null)
        {
            byte[] b = new byte[bits / 8];
            fis.read(b);
            r = new SecretKeySpec(b, "AES");
        }
        else
        {
            // Do NOT seed
            // SecureRandom: automatically seeded from system entropy.
            SecureRandom secureRandom = new SecureRandom();
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(bits, secureRandom);
            r = kgen.generateKey();
            FileOutputStream fos = ctxt.openFileOutput(INFO_FILE, Context.MODE_PRIVATE);
            fos.write(r.getEncoded());
        }

        info = r;
        return r;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CLEANER
    // ///////////////////////////////////////////////////////////////////////////
    /*
     * Nuke a file with zero's.
     */
    public static void nukeFile(File source, long size) throws Exception
    {
        if (size <= 0)
        {
            size = source.length();
        }

        byte zeros[] = new byte[MAX_BUFFER_SIZE];
        OutputStream destroyFile = new FileOutputStream(source);
        long chunks = (size + MAX_BUFFER_SIZE - 1) / MAX_BUFFER_SIZE;

        for (long i = 0; i < chunks; i++)
        {
            destroyFile.write(zeros);
        }

        destroyFile.flush();
        destroyFile.close();
    }
}
