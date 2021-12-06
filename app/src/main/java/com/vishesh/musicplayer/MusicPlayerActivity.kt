package com.vishesh.musicplayer

import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vishesh.musicplayer.databinding.ActivityMusicPlayerBinding
import java.util.concurrent.TimeUnit


class MusicPlayerActivity : AppCompatActivity(), ItemClicked {
    private var mediaPlayer: MediaPlayer? = null
    lateinit var binding: ActivityMusicPlayerBinding
    private lateinit var musicList: MutableList<Music>
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: MusicAdapter
    private var currPosition: Int = 0
    private var state = false  //false -> Player Is stopped/ true-> player is playing


    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        musicList = mutableListOf()

        if (Build.VERSION.SDK_INT >= 23)
            checkPermissions()

        binding.fabPlay.setOnClickListener {
            play(currPosition)
        }
        binding.fabNext.setOnClickListener {
            mediaPlayer?.stop()
            state = false
            if (currPosition < musicList.size - 1)
                currPosition += 1
            play(currPosition)
        }
        binding.fabPrevious.setOnClickListener {
            mediaPlayer?.stop()
            state = false
            if (currPosition > 0)
                currPosition -= 1
            play(currPosition)

        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }

    private fun play(currPosition: Int) {
        if (!state) {
            binding.fabPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))

            state = true
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(this@MusicPlayerActivity, Uri.parse(musicList[currPosition].songUri))
                prepare()
                start()
            }
            val mHandler = Handler()
            this@MusicPlayerActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    val playerPosition = mediaPlayer?.currentPosition!! / 1000
                    val totalDuration = mediaPlayer?.duration!! / 1000

                    binding.seekBar.max = totalDuration
                    binding.seekBar.progress = playerPosition

                    binding.pastTextView.text = timeFormat(playerPosition.toLong())
                    binding.remainTextView.text =
                        timeFormat((totalDuration - playerPosition).toLong())
                    mHandler.postDelayed(this, 1000)
                }
            })
        } else {
            state = false
            mediaPlayer?.stop()
            binding.fabPlay.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow))
        }
    }

    fun timeFormat(time: Long): String {
        val result = String.format(
            "%02d:%02d",
            TimeUnit.SECONDS.toMinutes(time),
            TimeUnit.SECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.SECONDS.toMinutes(time)
            )
        )
        var convert = ""
        for (i in 0 until result.length)
            convert += result[i]
        return convert

    }

    private fun getSongs() {
        val selection = MediaStore.Audio.Media.IS_MUSIC
        val projection = arrayOf(
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA
        )
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            musicList.add(
                Music(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
            )
        }
        cursor.close()

        linearLayoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(musicList, this)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = adapter

    }


    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getSongs()//read the Songs
        } else {
            //false When user asked not to ask me anymore/permission denied
            //true when rejected before but want to use it again
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(this, "Music Player Needs Access To Your Files", Toast.LENGTH_SHORT)
                    .show()
            }
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE
            )
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_READ_EXTERNAL_STORAGE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSongs()//read songs
            } else {
                Toast.makeText(this, "Permission Is Not Granted", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun itemClicked(position: Int) {
        mediaPlayer?.stop()
        state = false
        this.currPosition = position
        play(currPosition)
    }

}