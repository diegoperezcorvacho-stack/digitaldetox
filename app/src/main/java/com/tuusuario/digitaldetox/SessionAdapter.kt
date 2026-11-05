package com.tuusuario.digitaldetox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class SessionAdapter(private var sessions: List<SessionRecord>) :
    RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRecordId: TextView = itemView.findViewById(R.id.tvRecordId)
        val tvBadgeType: TextView = itemView.findViewById(R.id.tvBadgeType)
        val tvRecordDate: TextView = itemView.findViewById(R.id.tvRecordDate)
        val tvRecordDuration: TextView = itemView.findViewById(R.id.tvRecordDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session_record, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        val context = holder.itemView.context

        holder.tvRecordId.text = String.format(Locale.getDefault(), "ID #%d", session.id)
        holder.tvRecordDate.text = session.formattedDate

        val seconds = (session.durationMs / 1000) % 60
        val minutes = (session.durationMs / (1000 * 60)) % 60
        val hours = (session.durationMs / (1000 * 60 * 60))
        holder.tvRecordDuration.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

        if (session.isDemo) {
            holder.tvBadgeType.text = context.getString(R.string.tab_demo)
            holder.tvBadgeType.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_demo))
            holder.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.badge_demo_text))
        } else {
            holder.tvBadgeType.text = context.getString(R.string.tab_real)
            holder.tvBadgeType.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_real))
            holder.tvBadgeType.setTextColor(ContextCompat.getColor(context, R.color.badge_real_text))
        }
    }

    override fun getItemCount(): Int = sessions.size

    fun updateData(newSessions: List<SessionRecord>) {
        sessions = newSessions
        notifyDataSetChanged()
    }
}
