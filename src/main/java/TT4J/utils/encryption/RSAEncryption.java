package TT4J.utils.encryption;

import TT4J.interfaces.Encrypter;
import TT4J.utils.ConfigurationLoader;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.security.PrivateKey;
import java.security.PublicKey;


/**
 * Created by Stokowiec on 2015-07-01.
 */
public class RSAEncryption  implements Encrypter {

    private final Cipher cipher;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public RSAEncryption(ConfigurationLoader loader) throws Exception {
        this.cipher = Cipher.getInstance("RSA");
        this.publicKey =  loader.getPublicKey();
        this.privateKey = loader.getPrivateKey();
    }

    @Override
    public String encrypt(String inputString) throws Exception {
        this.cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] bytes = inputString.getBytes("UTF-8");
        byte[] encrypted = blockCipher(bytes, Cipher.ENCRYPT_MODE);

        char[] encryptedTranspherable = Hex.encodeHex(encrypted);
        return new String(encryptedTranspherable);
    }

    public String decrypt(String encrypted) throws Exception{
        this.cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] bts = Hex.decodeHex(encrypted.toCharArray());
        byte[] decrypted = blockCipher(bts,Cipher.DECRYPT_MODE);

        return new String(decrypted,"UTF-8");
    }

    public PrivateKey getPrivateKey(){
        return privateKey;
    }

    private byte[] blockCipher(byte[] bytes, int mode) throws IllegalBlockSizeException, BadPaddingException {
        // string initialize 2 buffers.
        // scrambled will hold intermediate results
        byte[] scrambled = new byte[0];

        // toReturn will hold the total result
        byte[] toReturn = new byte[0];
        // if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
        int length = (mode == Cipher.ENCRYPT_MODE)? 100 : 128;

        // another buffer. this one will hold the bytes that have to be modified in this step
        byte[] buffer = new byte[length];

        for (int i=0; i< bytes.length; i++){

            // if we filled our buffer array we have our block ready for de- or encryption
            if ((i > 0) && (i % length == 0)){
                //execute the operation
                scrambled = cipher.doFinal(buffer);
                // add the result to our total result.
                toReturn = append(toReturn,scrambled);
                // here we calculate the length of the next buffer required
                int newLength = length;

                // if newLength would be longer than remaining bytes in the bytes array we shorten it.
                if (i + length > bytes.length) {
                    newLength = bytes.length - i;
                }
                // clean the buffer array
                buffer = new byte[newLength];
            }
            // copy byte into our buffer.
            buffer[i%length] = bytes[i];
        }

        // this step is needed if we had a trailing buffer. should only happen when encrypting.
        // example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
        scrambled = cipher.doFinal(buffer);

        // final step before we can return the modified data.
        toReturn = append(toReturn,scrambled);

        return toReturn;
    }

    private byte[] append(byte[] prefix, byte[] suffix){
        byte[] toReturn = new byte[prefix.length + suffix.length];
        for (int i=0; i< prefix.length; i++){
            toReturn[i] = prefix[i];
        }
        for (int i=0; i< suffix.length; i++){
            toReturn[i+prefix.length] = suffix[i];
        }
        return toReturn;
    }
}
