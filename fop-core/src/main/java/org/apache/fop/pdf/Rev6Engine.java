/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.pdf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * AES-256 encryption with the hardened password hash (V5, R6) as defined in ISO 32000-2.
 */
final class Rev6Engine {

    private Rev6Engine() {
    }

    /**
     * Algorithm 2.B (ISO 32000-2, 7.6.4.3.4): computes a hash for R6 from the password,
     * an 8 byte salt and the 48 byte U value (owner) or an empty array (user).
     */
    static byte[] computeHash(byte[] password, byte[] salt, byte[] userKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(password);
            digest.update(salt);
            digest.update(userKey);
            byte[] hash = digest.digest();
            byte[] encrypted;
            int round = 0;
            do {
                int blockLength = password.length + hash.length + userKey.length;
                byte[] input = new byte[blockLength * 64];
                for (int i = 0; i < 64; i++) {
                    int offset = i * blockLength;
                    System.arraycopy(password, 0, input, offset, password.length);
                    System.arraycopy(hash, 0, input, offset + password.length, hash.length);
                    System.arraycopy(userKey, 0, input, offset + password.length + hash.length,
                            userKey.length);
                }
                encrypted = PDFEncryptionJCE.encryptWithKey(Arrays.copyOf(hash, 16), input, true,
                        Arrays.copyOfRange(hash, 16, 32));
                int sum = 0;
                for (int i = 0; i < 16; i++) {
                    sum += encrypted[i] & 0xff;
                }
                switch (sum % 3) {
                    case 0:
                        hash = MessageDigest.getInstance("SHA-256").digest(encrypted);
                        break;
                    case 1:
                        hash = MessageDigest.getInstance("SHA-384").digest(encrypted);
                        break;
                    default:
                        hash = MessageDigest.getInstance("SHA-512").digest(encrypted);
                        break;
                }
                round++;
            } while (round < 64 || (encrypted[encrypted.length - 1] & 0xff) > round - 32);
            return Arrays.copyOf(hash, 32);
        } catch (NoSuchAlgorithmException exception) {
            throw new UnsupportedOperationException(exception);
        }
    }
}
