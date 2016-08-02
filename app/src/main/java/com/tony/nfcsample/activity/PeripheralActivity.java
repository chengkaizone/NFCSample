package com.tony.nfcsample.activity;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.tony.nfcsample.R;

import java.util.Locale;

/**
 * Created by lance on 16/8/2.
 * 作为外围设备
 */
public class PeripheralActivity extends AppCompatActivity {
    final String TAG = "PeripheralActivity";

    private NfcAdapter mNfcAdapter;
    private TextView tv_message;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_peripheral);
        tv_message = (TextView) findViewById(R.id.tv_message);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            tv_message.setText("设备不支持NFC");
        } else if (mNfcAdapter.isEnabled() == false){
            tv_message.setText("请在系统设置中先启用NFC功能");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent())) {
            String str = processIntent(getIntent());
            tv_message.setText("nfc: " + str);
        }

    }

    // 将字符串序列转化为16进制字符串
    private String bytesToHexString(byte[] src) {

        if (src == null || src.length == 0) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder("0x");
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
            Log.i(TAG, "append: " + stringBuilder.toString());
        }

        return stringBuilder.toString();
    }

    // 处理传递过来的数据
    private String processIntent(Intent intent) {
        //取出封装在intent中的TAG
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        for (String tech : tag.getTechList()) {
            System.out.println(tech);
            Log.i(TAG, "tech: " + tech);
        }
        boolean auth = false;
        //读取TAG
        MifareClassic mfc = MifareClassic.get(tag);
        try {
            String metaInfo = "";
            //Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";
            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_DEFAULT);
                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";
                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }
            return metaInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    private String readFromTag(Intent intent) {
        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg = (NdefMessage) rawArray[0];
        NdefRecord ndefRecord = msg.getRecords()[0];

        try {
            if (ndefRecord != null) {
                String result = new String(ndefRecord.getPayload(), "UTF-8");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // 写入消息到nfc设备中
//    private void writeNdefMessage(String msg, Intent intent) {
//
//        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        Ndef ndef = Ndef.get(tag);
//
//        try {
//            ndef.connect();
//            NdefRecord ndefRecord = NdefRecord.createTextRecord("", Locale.US, true);
//            NdefRecord[] records = { ndefRecord };
//            NdefMessage ndefMessage = new NdefMessage(records);
//
//            ndef.writeNdefMessage(ndefMessage);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }


}
