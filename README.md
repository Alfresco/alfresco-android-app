Welcome to the Alfresco Android Application
===================================

Alfresco Mobile connects to Alfresco in the cloud and Alfresco on premise servers (3.4 and above) to provide safe access to your corporate documents on the go. 

Alfresco Mobile connects to your Alfresco repository using your Alfresco log-in credentials (over HTTP or HTTPS), and manages user access based on their permissions.

Alfresco Mobile is based on top of our Alfresco Android SDK. More information on Alfresco Android SDK can be found on our [developer portal](http://developer.alfresco.com/mobile).


Releases
--------

The master branch is used for development of new features so it's stability can not be guaranteed, for the current stable release 
[download the pre-built binaries](https://play.google.com/store/apps/details?id=org.alfresco.mobile.android.application) from the Google Play Store. 
Alternatively, use one of the [tags](https://github.com/Alfresco/alfresco-android-app/tags) to build from source.

Prerequisites
-------------

* Android Maven SDK Deployer

To build this project with Maven, you'll need to use the Android Maven SDK Deployer (https://github.com/mosabua/maven-android-sdk-deployer) in order to install the Google Support Library APIs into your local Maven repository.
To import, simply go to the maven-android-sdk-deployer root directory and execute a mvn install.

Important Note : Be sure you already installed all the relevant Android packages to your local SDK installation. 
You can find more informations at https://github.com/mosabua/maven-android-sdk-deployer/blob/master/README.markdown.


Optional
-------------

* Samsung Mobile SDK


License
-------

Alfresco Mobile for Android 1.4 

Copyright © 2014 Alfresco Software, Ltd. and others. 

This product distribution is made available under the [Apache 2.0 license] (http://www.apache.org/licenses/LICENSE-2.0.html). 

Portions of this product distribution require certain copyright or other notices to be included with the product distribution. These notices may also appear in individual source files. 

Below is the list of licenses and modules used under the corresponding licenses: 

__Apache 2.0 License__

* Android Open Source Project	 http://source.android.com/

* OkHttp	 https://github.com/square/okhttp

* DiskLruCache	 https://github.com/JakeWharton/DiskLruCache 

* Picasso	 http://square.github.io/picasso/

* Android Split Pane Layout	 https://github.com/MobiDevelop/android-split-pane-layout 

__MIT License__

* Image View Zoom	 https://github.com/sephiroth74/ImageViewZoom
