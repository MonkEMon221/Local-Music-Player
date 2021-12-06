package com.vishesh.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MusicAdapter(
    private var musicList: MutableList<Music>,
    private var itemClicked: ItemClicked
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private lateinit var music: Music
        private var artistname: TextView
        private var songName: TextView

        init {
            artistname = view.findViewById(R.id.artist_textView)
            songName = view.findViewById(R.id.song_textView)

            view.setOnClickListener(this)
        }

        fun bindMusic(music: Music) {
            this.music = music
            artistname.text = music.artistName
            songName.text = music.songName
        }

        override fun onClick(v: View?) {
            itemClicked.itemClicked(adapterPosition)
        }

    }

    override fun onCreateViewHolder(viewgroup: ViewGroup, viewType: Int): MusicViewHolder {
        val context = viewgroup.context
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false

        val view =
            inflater.inflate(R.layout.music_items, viewgroup, shouldAttachToParentImmediately)
        return MusicViewHolder(view)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val item = musicList[position]
        holder.bindMusic(item)
    }

}