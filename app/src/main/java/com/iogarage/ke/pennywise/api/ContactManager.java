package com.iogarage.ke.pennywise.api;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iogarage.ke.pennywise.R;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by choxx on 4/26/14.
 */
public class ContactManager {

    public static final String TAG = "ContactsAdder";


    /**
     * Creates a contact entry from the current UI values in the account named by mSelectedAccount.
     */
    public static void createContactEntry(Context context, String name, String phoneNumber) {
        // Get values from UI
        int phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;

        // Prepare contact creation request
        //
        // Note: We use RawContacts because this data must be associated with a particular account.
        //       The system will aggregate this with any other data for this contact and create a
        //       coresponding entry in the ContactsContract.Contacts provider for us.
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, ContactsContract.RawContacts.ACCOUNT_TYPE)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, ContactsContract.RawContacts.ACCOUNT_TYPE)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
                .build());

        // Ask the Contact provider to create a new contact

        Log.i(TAG, "Creating contact: " + name);

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            // Display warning

            CharSequence txt = context.getString(R.string.contactCreationFailure);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, txt, duration);
            toast.show();

            // Log exception
            Log.e(TAG, "Exceptoin encoutered while inserting contact: " + e);
        }
    }

    public static Bitmap getPhoto(Context context, String phoneNumber) {

        if(TextUtils.isEmpty(phoneNumber)){
            Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown);
            return defaultPhoto;
        }


        Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Uri photoUri = null;
        ContentResolver cr = context.getContentResolver();
        Cursor contact = cr.query(phoneUri,
                new String[]{ContactsContract.Contacts._ID}, null, null, null);

        if (contact.moveToFirst()) {
            long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
            photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);

        } else {
            Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown);
            contact.close();
            return defaultPhoto;
        }
        if (photoUri != null) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                    cr, photoUri);
            if (input != null) {
                contact.close();
                return BitmapFactory.decodeStream(input);
            }
        } else {
            Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown);
            contact.close();
            return defaultPhoto;
        }
        Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown);
        contact.close();
        return defaultPhoto;
    }

}

