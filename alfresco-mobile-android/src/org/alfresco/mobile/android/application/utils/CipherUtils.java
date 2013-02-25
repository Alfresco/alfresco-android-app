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

package org.alfresco.mobile.android.application.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.preferences.Prefs;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;


public class CipherUtils
{
    private static final byte[] SALT = { 0x0A, 0x02, 0x13, 0x3C, 0x3B, 0x0F, 0x1A };
    private static final int COUNT = 10;
    private static final byte[] REFERENCE_DATA = "AlfrescoCrypto".getBytes();
    private static final String ALGORITHM = "PBEWithMD5AndDES";
    private static final int KEY_LENGTH = 128;
    private static final String INFO_FILE = "tmp.qzv";
    private static int chunkSize = 0;
    private static SecretKey info = null;
    
    static SecretKey generateKey(Context ctxt, int bits) throws IOException, NoSuchAlgorithmException
    {
        if (info != null)
            return info;
        
        SecretKey r;
        FileInputStream fis = null;
        try
        {
            fis = ctxt.openFileInput(INFO_FILE);
        }
        catch (FileNotFoundException e) { fis = null; }
        
        if (fis != null)
        {
            byte[] b = new byte[bits/8];
            fis.read(b);
            r = new SecretKeySpec(b, "AES");
        }
        else
        {
            SecureRandom secureRandom = new SecureRandom(); //Do NOT seed SecureRandom: automatically seeded from system entropy.
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(bits, secureRandom);
            r = kgen.generateKey();
            FileOutputStream fos = ctxt.openFileOutput(INFO_FILE, Context.MODE_PRIVATE);
            fos.write(r.getEncoded());
        }
        
        info = r;
        return r;
    }
    
    /*
     * Encrypt file in place, leaving original file unencrypted.
     */
    public static boolean encryptFile(Context ctxt, String filename, boolean nuke) throws Exception
    {
        return encryptFile (ctxt, filename, null, nuke);
    }
    
    /*
     * Encrypt file in place, leaving no trace of original unencrypted data.
     * 
     * filename     file to encrypt
     * nuke         whether to zero the original unencrypted file before attempting its deletion, for additional security.
     * 
     */
    public static boolean encryptFile(Context ctxt, String filename, String newFilename, boolean nuke) throws Exception
    {
        if (!isEncrypted(ctxt, filename, true))
        {
            boolean ret = true;
            File source = new File (filename);
            File dest = new File (newFilename != null ? newFilename : filename + ".etmp");
            InputStream sourceFile = new FileInputStream(source);
            OutputStream destFile = wrapCipherOutputStream(new FileOutputStream(dest), generateKey(ctxt, 128).toString());
            int nBytes = 0;
            long size = 0;
            
            chunkSize = 10240;
            byte buffer[] = new byte[chunkSize];
            
            do
            {
                size += (nBytes = sourceFile.read(buffer));
                
                if (nBytes > 0)
                {
                    destFile.write(buffer);
                }
            }
            while (nBytes > 0);
            
            sourceFile.close();
            destFile.flush();
            destFile.close();
            
            Log.e("Alfresco", "Encryption phase succeeded for file " + source.getName());
            
            if (newFilename == null)
            {
                if (nuke)
                    nukeFile (source, size);
                
                if (source.delete())
                {
                    //Rename encrypted file to original name.
                    if (false == (ret=dest.renameTo(source)))
                        Log.e("Alfresco", "Cannot rename encrypted file " + dest.getName());
                }
                else
                {
                    Log.e("Alfresco", "Cannot delete original file " + source.getName());
                    
                    dest.delete();
                    ret = false;
                }
            }
            
            return ret;
        }
        else
            return true;
    }
    
    /*
     * Decrypt file in place, leaving original file unencrypted.
     */
    public static boolean decryptFile(Context ctxt, String filename) throws Exception
    {
        return decryptFile (ctxt, filename, null);
    }
    
    /*
     * Decrypt file, either in place, or to a new filename.
     * 
     * filename     file to decrypt.
     * newFilename  file to decrypt to, or null to decrypt in place.
     */
    public static boolean decryptFile(Context ctxt, String filename, String newFilename) throws Exception
    {
        if (isEncrypted(ctxt, filename, true))
        {
            boolean ret = true;
            File source = new File (filename);
            File dest = new File (newFilename != null ? newFilename : filename + ".utmp");
            InputStream sourceFile = wrapCipherInputStream(new FileInputStream(source), generateKey(ctxt, 128).toString());
            OutputStream destFile = new FileOutputStream(dest);
            int nBytes = 0;
     
            chunkSize = 10240;
            byte buffer[] = new byte[chunkSize];
            
            do
            {
                nBytes = sourceFile.read(buffer);
                
                if (nBytes > 0)
                {
                   destFile.write(buffer);
                }
            }
            while (nBytes > 0);
            
            sourceFile.close();
            destFile.close();
            
            Log.e("Alfresco", "Decryption phase succeeded for file " + source.getName());
            
            if (newFilename == null)
            {
                if (source.delete())
                {
                    //Rename decrypted file to original name.
                    if (false == (ret=dest.renameTo(source)))
                        Log.e("Alfresco", "Cannot rename decrypted file " + dest.getName());
                }
                else
                {
                    Log.e("Alfresco", "Cannot delete original file " + source.getName());
                    dest.delete();
                    ret = false;
                }
            }
            
            return ret;
        }
        else
            return true;
    }
        
    public static boolean isEncryptionActive (Context ctxt)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        return prefs.getBoolean(Prefs.PRIVATE_FOLDERS, false);
    }
    
    public static boolean isEncrypted (Context ctxt, String filename, boolean fullTest) throws IOException
    {
        File source = new File (filename);
        InputStream sourceFile = null;
        try
        {
            sourceFile = testInputStream(new FileInputStream(source), generateKey(ctxt, 128).toString());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        
        if (sourceFile != null)
        {
            sourceFile.close();
            return true;
        }
        
        return false;
    }
    
    public static OutputStream wrapCipherOutputStream(OutputStream streamOut, String password) throws IOException, GeneralSecurityException
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

    public static InputStream wrapCipherInputStream(InputStream streamIn, String password) throws IOException, GeneralSecurityException 
    {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(SALT, COUNT);

        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
        pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

        /*
         * Read a predefined data block. If the password is incorrect, we'll get a security
         * exception here. Without this, we will only get an IOException later when reading
         * the CipherInputStream, which is not specific enough for a good error message.
         */
        int count = streamIn.read();
        if (count <= 0 || count > 1024)
        {
            throw new IOException("Bad encrypted file");
        }

        byte[] input = new byte[count];
        streamIn.read(input);
        pbeCipher.doFinal(input);

        return new CipherInputStream(streamIn, pbeCipher);
    }
    
    public static InputStream testInputStream(InputStream streamIn, String password)
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
            if (count <= 0 || count > 1024)
            {
                return null;
            }
    
            byte[] input = new byte[count];
            streamIn.read(input);
            pbeCipher.doFinal(input).toString().contains(REFERENCE_DATA.toString());
    
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
    
    /*
     * Nuke a file with zero's.
     */
    public static void nukeFile (File source, long size) throws Exception
    {
        if (size <= 0) size = source.length();
        
        byte zeros[] = new byte[chunkSize];
        OutputStream destroyFile = new FileOutputStream(source);
        long chunks = (size + chunkSize - 1 ) / chunkSize;
        
        for (long i = 0;  i < chunks;  i++)
            destroyFile.write (zeros);
        
        destroyFile.flush();
        destroyFile.close();
    }
    
    public static void EncryptionUserInteraction (final Context ctxt)
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);

        if (!prefs.getBoolean(Prefs.ENCRYPTION_USER_INTERACTION, false)  &&  !prefs.getBoolean(Prefs.PRIVATE_FOLDERS, false) )
        {
            final File folder = StorageManager.getPrivateFolder(ctxt, "", "", "");
            if (folder != null)
            {
                prefs.edit().putBoolean (Prefs.ENCRYPTION_USER_INTERACTION, true).commit();
                
                AlertDialog.Builder builder = new AlertDialog.Builder(ctxt);
                builder.setTitle(ctxt.getString(R.string.data_protection));
                builder.setMessage(ctxt.getString(R.string.content_being_encrypted));
                builder.setCancelable(false);
                final AlertDialog progressAlert = builder.create();
                
                builder = new AlertDialog.Builder(ctxt);
                builder.setTitle(ctxt.getString(R.string.data_protection));
                builder.setMessage(ctxt.getString(R.string.data_protection_blurb));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int item)
                    {
                        dialog.dismiss();
                        progressAlert.show();
                        
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Vector<String> folders = new Vector<String>();
                                folders.add(StorageManager.DLDIR);
                                folders.add(StorageManager.TEMPDIR);
                                
                                if (IOUtils.encryptFiles(ctxt, folder.getPath(), folders, true))
                                    prefs.edit().putBoolean(Prefs.PRIVATE_FOLDERS, true).commit();
                                else
                                    MessengerManager.showLongToast(ctxt, ctxt.getString(R.string.encryption_failed));
                                
                                progressAlert.hide();
                            }
                            
                        }, 1000);
                    }
                });
                
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int item)
                    {
                        dialog.dismiss();
                    }
                });
                
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
}
