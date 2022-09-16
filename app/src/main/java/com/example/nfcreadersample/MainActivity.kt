package com.example.nfcreadersample

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nfcreadersample.databinding.ActivityMainBinding
import java.math.BigInteger


class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var binding: ActivityMainBinding
    private var nfcAdapter: NfcAdapter? = null
    private val TAG = "layon.f"
    private lateinit var pendingIntent : PendingIntent
    private var tagDiscoveryCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        checkIfHasNFCHardware()
    }

    private fun addRowOnTable(s1 : String, s2 : String, s3: String) {
        /* Create a new row to be added. */
        val tr = TableRow(this)
        tr.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        /* Create a TextView to be the row-content. */
        val t1 = TextView(this)
        t1.text = s1
        t1.layoutParams = TableRow.LayoutParams(0).apply {
            this.gravity = Gravity.CENTER
        }

        val t2 = TextView(this)
        t2.text = s2
        t2.layoutParams = TableRow.LayoutParams(1).apply {
            this.gravity = Gravity.CENTER
        }

        val t3 = TextView(this)
        t3.text = s3
        t3.layoutParams = TableRow.LayoutParams(2).apply {
            this.gravity = Gravity.CENTER
        }

        /* Add TextView to row. */
        tr.addView(t1)
        tr.addView(t2)
        tr.addView(t3)

        /* Add row to TableLayout. */
        if(tagDiscoveryCount % 2 == 0){
            tr.setBackgroundColor(resources.getColor(R.color.tableRow, null))
        }

        binding.table.addView(
            tr,
            TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        nfcAdapter?.disableReaderMode(this)
    }

    fun checkIfHasNFCHardware(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null || (nfcAdapter?.isEnabled == false)) {
            //NFC is not available or enabled
            binding.title2.text = "NFC is not available or enabled"
        } else {
            //setupNfcReaderModeBackground() //setup the foreground reader
            startBlinkEffect(binding.reading)
            enableReadTagByOnTagDiscovered()
        }
    }

    fun startBlinkEffect(text : TextView) {
        val animation = AlphaAnimation(0.1f, 1.0f).also {
            it.duration = 250
            it.repeatMode = Animation.REVERSE
            it.repeatCount = Animation.INFINITE
        }
        text.startAnimation(animation)
    }

    private fun enableReadTagByOnNewIntent() {
         pendingIntent = PendingIntent.getActivity(
             this,
             0,
             Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
             PendingIntent.FLAG_MUTABLE
         )
         nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    private fun enableReadTagByOnTagDiscovered() {
        nfcAdapter?.enableReaderMode(
            this, this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
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

        val biStr = BigInteger(inarray)
        val binary = biStr.toString(2)
        val hexadecimal = biStr.toString(16).uppercase()
        val decimal = biStr.toString(10)

        Log.d(TAG, "ByteArrayToHexString binary = $binary")
        Log.d(TAG, "ByteArrayToHexString hex = $hexadecimal")
        Log.d(TAG, "ByteArrayToHexString dec = $decimal")

        return hexadecimal
    }

    override fun onTagDiscovered(tag: Tag?) {
        Log.d("layon.f", "onTagDiscovered: $tag")
        tag?.let {
            tagDiscoveryCount++
            val id = Utils.toHex(it.id)
            var responseByte : ByteArray? = null
            var responseString : String = "           -           "
            var techs : String = ""
            it.techList.forEach {
                techs = "$techs${if(techs.isNotEmpty()) "," else ""} ${it.split(".").last()}"
            }

            val isoDep = IsoDep.get(tag)
            try {
                isoDep.connect()
                responseByte = isoDep.transceive(
                    Utils.hexStringToByteArray(
                        "00A4040007F0010203040506"
                    )
                )
                responseString = String(responseByte)
                isoDep.close()
            } catch (e: Exception) {
                Log.d("layon.f", "onTagDiscovered error: $e")
            } finally {
                runOnUiThread {
                    addRowOnTable(id, techs, responseString)
                }
            }
        }
    }
}