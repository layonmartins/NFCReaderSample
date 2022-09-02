package com.example.nfcreadersample

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.nfcreadersample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null
    private val TAG = "layon.f"
    private lateinit var pendingIntent : PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        checkIfHasNFCHardware()
        checkIfDeviceCanEmulateHostNFCTag()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        nfcAdapter?.disableReaderMode(this)
    }

    fun checkIfDeviceCanEmulateHostNFCTag(){
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)){
            binding.hasNfcHostEmulatorTextView.visibility = View.VISIBLE
        }
    }

    fun checkIfHasNFCHardware(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null || (nfcAdapter?.isEnabled == false)) {
            //NFC is not available or enabled
            binding.hasNfcTextView.text = "NFC is not available or enabled"
            binding.imageView.visibility = View.GONE
        } else {
            //setupNfcReaderModeBackground() //setup the foreground reader
            Log.d(TAG, "device can read NFC")

            //If device can read NFC
            pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
            )
            nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
        }
    }

    //used to make the tag opens on background
    private fun setupNfcReaderModeBackground() {

        val options = Bundle()
        // Work around for some broken Nfc firmware implementations that poll the card too fast
        // Work around for some broken Nfc firmware implementations that poll the card too fast
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

        nfcAdapter?.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_B or
        NfcAdapter.FLAG_READER_NFC_F or
        NfcAdapter.FLAG_READER_NFC_V or
        NfcAdapter.FLAG_READER_NFC_BARCODE,
        options
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolveIntent(intent)
    }

    private fun resolveIntent(intent : Intent?) {

        Log.d(TAG, "resolveIntent = $intent")

        val action = intent?.action
        Log.d(TAG, "resolveIntent action = $action")
        if(NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action) {

            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                // Process the messages array.
                Log.d(TAG, "resolveIntent messages = $messages")
                    messages.forEach {
                        Log.d(TAG, "resolveIntent message = $it")

                    }
            }

            val tag : Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            Log.d(TAG, "resolveIntent tag = $tag")
            tag?.let {
                Log.d(TAG, "resolveIntent tag.id = ${tag.id}")
                Log.d(TAG, "resolveIntent tag.id Hex = ${ByteArrayToHexString(tag.id)}")
            }

            val uid = intent?.getByteArrayExtra(NfcAdapter.EXTRA_ID)
            uid?.let {
                val uidHex = ByteArrayToHexString(uid)
                Log.d(TAG, "resolveIntent uidHex = $uidHex")
                binding.hasNfcIdTextView.apply {
                    visibility = View.VISIBLE
                    text = "UID: $uidHex"
                }
            }
            Log.d(TAG, "resolveIntent uid = $uid")

            val aid = intent?.getByteArrayExtra(NfcAdapter.EXTRA_AID)
            Log.d(TAG, "resolveIntent aid = $aid")

            val data = intent?.getByteArrayExtra(NfcAdapter.EXTRA_DATA)
            Log.d(TAG, "resolveIntent data = $data")

            val secure = intent?.getByteArrayExtra(NfcAdapter.EXTRA_SECURE_ELEMENT_NAME)
            Log.d(TAG, "resolveIntent secure = $secure")

            val message = intent?.getByteArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            Log.d(TAG, "resolveIntent message = $message")

            val state = intent?.getByteArrayExtra(NfcAdapter.EXTRA_ADAPTER_STATE)
            Log.d(TAG, "resolveIntent state = $state")

            val paymentPreferred = intent?.getByteArrayExtra(NfcAdapter.EXTRA_PREFERRED_PAYMENT_CHANGED_REASON)
            Log.d(TAG, "resolveIntent paymentPreferred = $paymentPreferred")

            val readerDelay = intent?.getByteArrayExtra(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY)
            Log.d(TAG, "resolveIntent readerDelay = $readerDelay")

        }
    }

    //conver the array of byte to hexadecimal String
    fun ByteArrayToHexString(inarray: ByteArray): String? {
        var i: Int
        var j: Int
        var `in`: Int
        val hex =
            arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
        var out = ""
        j = 0
        while (j < inarray.size) {
            `in` = inarray[j].toInt() and 0xff
            i = `in` shr 4 and 0x0f
            out += hex[i]
            i = `in` and 0x0f
            out += hex[i]
            ++j
        }
        return out
    }

    override fun onTagDiscovered(tag: Tag?) {
        Log.d(TAG, "onTagDiscovered tag = $tag")
    }


}