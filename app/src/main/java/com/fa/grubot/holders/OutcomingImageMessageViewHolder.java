package com.fa.grubot.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fa.grubot.R;
import com.fa.grubot.objects.chat.ChatImageMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OutcomingImageMessageViewHolder extends MessageHolders.IncomingTextMessageViewHolder<ChatImageMessage> {
    @BindView(R.id.image) ImageView image;
    @BindView(R.id.messageText) TextView messageText;
    @BindView(R.id.messageTime) TextView messageTime;

    public OutcomingImageMessageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(ChatImageMessage message) {
        super.onBind(message);

        this.getImageLoader().loadImage(image, message.getImgUri());
        messageText.setText(message.getText());
        messageTime.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
    }
}
