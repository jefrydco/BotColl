package id.jefrydco.botcoll.message;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import id.jefrydco.botcoll.R;
import id.jefrydco.botcoll.message.model.BaseMessage;
import id.jefrydco.botcoll.message.model.BotMessage;
import id.jefrydco.botcoll.message.model.UserMessage;
import id.jefrydco.botcoll.utils.DateUtils;
import id.jefrydco.botcoll.utils.ImageUtils;

public class MessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "MessageListAdapter";

    private static final int VIEW_TYPE_BOT_MESSAGE = 0;
    private static final int VIEW_TYPE_USER_MESSAGE = 1;

    private Context mContext;
    private List<BaseMessage> mBaseMessageList;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    interface OnItemClickListener {
        void onBotMessageItemClick(BotMessage botMessage, int position);

        void onUserMessageItemClick(UserMessage userMessage, int position);
    }

    interface OnItemLongClickListener {
        void onBaseMessageLongClick(BaseMessage baseMessage, int position);
    }

    MessageListAdapter(Context mContext, List<BaseMessage> baseMessageList) {
        this.mContext = mContext;
        this.mBaseMessageList = baseMessageList;
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_BOT_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_bot, parent, false);
            return new BotMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (mBaseMessageList.get(position) instanceof BotMessage) {
            return VIEW_TYPE_BOT_MESSAGE;
        } else if (mBaseMessageList.get(position) instanceof UserMessage) {
            return VIEW_TYPE_USER_MESSAGE;
        }

        return -1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseMessage baseMessage = mBaseMessageList.get(position);

        boolean isNewDay = false;

        if (position < mBaseMessageList.size() - 1) {
            BaseMessage prevBaseMessage = mBaseMessageList.get(position + 1);

            if (!DateUtils.hasSameDate(baseMessage.getCreatedAt(), prevBaseMessage.getCreatedAt())) {
                isNewDay = true;
            }
        } else if (position == mBaseMessageList.size() - 1) {
            isNewDay = true;
        }

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_BOT_MESSAGE:
                ((BotMessageViewHolder) holder).bind(mContext, isNewDay, (BotMessage) baseMessage,
                        mItemClickListener,
                        mItemLongClickListener,
                        position);
                break;
            case VIEW_TYPE_USER_MESSAGE:
                ((UserMessageViewHolder) holder).bind(mContext, isNewDay, (UserMessage) baseMessage,
                        mItemClickListener,
                        mItemLongClickListener,
                        position);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mBaseMessageList.size();
    }

    void setBaseMessageList(List<BaseMessage> baseMessageList) {
        this.mBaseMessageList = baseMessageList;
        notifyDataSetChanged();
    }

    public List<BaseMessage> getBaseMessageList() {
        return mBaseMessageList;
    }

    void addFirst(BaseMessage baseMessage) {
        mBaseMessageList.add(0, baseMessage);
        notifyDataSetChanged();
    }

    void addLast(BaseMessage baseMessage) {
        mBaseMessageList.add(baseMessage);
        notifyDataSetChanged();
    }

    void delete(String id) {
        for (BaseMessage baseMessage : mBaseMessageList) {
            if (baseMessage.getId().equals(id)) {
                mBaseMessageList.remove(baseMessage);
                notifyDataSetChanged();
                break;
            }
        }
    }

    void update(BaseMessage baseMessage) {
        BaseMessage baseMessageTemp;
        for (int index = 0; index < mBaseMessageList.size(); index++) {
            baseMessageTemp = mBaseMessageList.get(index);
            if (baseMessage.getId().equals(baseMessageTemp.getId())) {
                mBaseMessageList.remove(index);
                mBaseMessageList.add(index, baseMessage);
                notifyDataSetChanged();
                break;
            }
        }
    }

    private class BotMessageViewHolder extends RecyclerView.ViewHolder {

        TextView mTextViewMessageSender, mTextViewMessageBody, mTextViewMessageTime;
        ImageView mImageViewMessageProfile;

        BotMessageViewHolder(View itemView) {
            super(itemView);
            mTextViewMessageSender = itemView.findViewById(R.id.item_message_bot_textView_message_sender);
            mTextViewMessageBody = itemView.findViewById(R.id.item_message_bot_textView_message_body);
            mTextViewMessageTime = itemView.findViewById(R.id.item_message_bot_textView_message_time);
            mImageViewMessageProfile = itemView.findViewById(R.id.item_message_bot_imageView_message_profile);
        }

        void bind(final Context context,
                  boolean isNewDay,
                  final BotMessage botMessage,
                  @Nullable final OnItemClickListener clickListener,
                  @Nullable final OnItemLongClickListener longClickListener,
                  final int position) {

            mTextViewMessageSender.setText(botMessage.getSender().getName());
            mTextViewMessageBody.setText(botMessage.getMessage());

            if (isNewDay) {
                mTextViewMessageTime.setVisibility(View.VISIBLE);
                mTextViewMessageTime.setText(DateUtils.formatDate(botMessage.getCreatedAt()));
            } else {
                mTextViewMessageTime.setVisibility(View.GONE);
            }

            ImageUtils.displayRoundImageFromUrl(context, botMessage.getSender().getProfileUrl(), mImageViewMessageProfile);

            if (clickListener != null) {
                itemView.setOnClickListener(v -> {
                    clickListener.onBotMessageItemClick(botMessage, position);
                });
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(v -> {
                    longClickListener.onBaseMessageLongClick(botMessage, position);
                    return true;
                });
            }
        }
    }

    private class UserMessageViewHolder extends RecyclerView.ViewHolder {

        TextView mTextViewMessageBody, mTextViewMessageTime;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            mTextViewMessageBody = itemView.findViewById(R.id.item_message_user_textView_message_body);
            mTextViewMessageTime = itemView.findViewById(R.id.item_message_user_textView_message_time);
        }

        void bind(final Context context,
                  boolean isNewDay,
                  final UserMessage userMessage,
                  @Nullable final OnItemClickListener clickListener,
                  @Nullable final OnItemLongClickListener longClickListener,
                  final int position) {

            mTextViewMessageBody.setText(userMessage.getMessage());

            if (isNewDay) {
                mTextViewMessageTime.setVisibility(View.VISIBLE);
                mTextViewMessageTime.setText(DateUtils.formatDate(userMessage.getCreatedAt()));
            } else {
                mTextViewMessageTime.setVisibility(View.GONE);
            }

            if (clickListener != null) {
                itemView.setOnClickListener(v -> {
                    clickListener.onUserMessageItemClick(userMessage, position);
                });
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(v -> {
                    longClickListener.onBaseMessageLongClick(userMessage, position);
                    return true;
                });
            }
        }
    }
}
