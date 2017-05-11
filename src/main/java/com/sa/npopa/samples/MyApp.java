package com.sa.npopa.samples;

import java.io.*;
import java.security.PrivilegedExceptionAction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.security.*;

class FileCount {
    public static void main(final String[] args) throws IOException, FileNotFoundException, InterruptedException{

        UserGroupInformation app_ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI("myapplication/myhost", "myapplication.keytab");
        UserGroupInformation proxy_ugi = UserGroupInformation.createProxyUser("myuser", app_ugi);

        proxy_ugi.doAs( new PrivilegedExceptionAction() {
            public Void run() throws Exception {
            	//do something here as user

                return null;
            }
        } );
    }
}