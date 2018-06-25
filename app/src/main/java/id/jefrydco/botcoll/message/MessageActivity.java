package id.jefrydco.botcoll.message;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.jefrydco.botcoll.R;
import id.jefrydco.botcoll.message.model.BaseMessage;
import id.jefrydco.botcoll.message.model.BotMessage;
import id.jefrydco.botcoll.message.model.UserMessage;
import id.jefrydco.botcoll.utils.NetworkUtils;
import id.jefrydco.botcoll.utils.ULID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";

    private static final int DATA_SIZE = 100;

    private ULID ulid = new ULID();

    private List<BaseMessage> mBaseMessageList;

    private RecyclerView mRecyclerViewMessageList;
    private MessageListAdapter mMessageListAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText mEditTextChatboxInput;
    private Button mButtonChatboxSend;
    private TextView mTextViewNewMessage;

    private MessageService mMessageService;
    private Call<String> mCall;
    private Callback<String> mCallback = new Callback<String>() {
        @Override
        public void onResponse(Call<String> call, Response<String> response) {
            String jsonResponse = response.body();

            try {
                JSONObject rootObject = new JSONObject(jsonResponse);
                JSONArray historyArray = rootObject.getJSONArray("history");

                if (historyArray.length() > 0) {

                    for (int index = 0; index < historyArray.length(); index++) {
                        JSONObject historyArrayItem = historyArray.getJSONObject(index);

                        ULID.Value botIdValue = ulid.nextValue();
                        String botId = botIdValue.toString();

                        String day = historyArrayItem.getString("hari");
                        int subject = historyArrayItem.getInt("matkul");
                        String room = historyArrayItem.getString("kelas");
                        String date = historyArrayItem.getString("tanggal");

                        mMessageListAdapter.addFirst(new BotMessage(
                                botId,
                                "Jadwal matkul hari " + day + " tanggal " + date + " di kelas" + room + " adalah " + subject,
                                System.currentTimeMillis()));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<String> call, Throwable t) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        setupAdapter();
        setupLayoutManager();
        setupRecyclerView();
        setupChatbox();
        setupNewMessage();
    }

    private void setupAdapter() {
        List<BaseMessage> baseMessageList = generateBaseMessageList(DATA_SIZE);

        mMessageListAdapter = new MessageListAdapter(this, baseMessageList);
        mMessageListAdapter.setOnItemClickListener(new MessageListAdapter.OnItemClickListener() {
            @Override
            public void onBotMessageItemClick(BotMessage botMessage, int position) {
                setupToast("Clicked: " + botMessage.getMessage() + " " + position);
            }

            @Override
            public void onUserMessageItemClick(UserMessage userMessage, int position) {
                setupToast("Clicked: " + userMessage.getMessage() + " " + position);
            }
        });

        mMessageListAdapter.setOnItemLongClickListener((baseMessage, position) ->
                setupToast("Long clicked: " + baseMessage.getMessage() + " " + position));
    }

    private void setupLayoutManager() {
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
    }

    private void setupRecyclerView() {
        mRecyclerViewMessageList = findViewById(R.id.activity_message_reyclerView_message_list);
        mRecyclerViewMessageList.setLayoutManager(mLinearLayoutManager);
        mRecyclerViewMessageList.setAdapter(mMessageListAdapter);
    }

    private void setupChatbox() {
        mEditTextChatboxInput = findViewById(R.id.activity_message_editText_chatbox_input);
        mButtonChatboxSend = findViewById(R.id.activity_message_button_chatbox_send);

        mButtonChatboxSend.setEnabled(false);

        mEditTextChatboxInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mButtonChatboxSend.setEnabled(true);
                } else {
                    mButtonChatboxSend.setEnabled(false);
                }
            }
        });

        mButtonChatboxSend.setOnClickListener(v -> {
            String message = mEditTextChatboxInput.getText().toString();
            if (message.length() > 0) {
                sendUserMessage(message);
                mEditTextChatboxInput.setText("");
            }
        });
    }

    private void setupNewMessage() {
        mTextViewNewMessage = findViewById(R.id.activity_message_textView_new_message);
        mTextViewNewMessage.setOnClickListener(v -> {
            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(0);
            mLinearLayoutManager.startSmoothScroll(smoothScroller);
            mTextViewNewMessage.setVisibility(View.GONE);
        });
    }

    private void setupToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void sendUserMessage(String message) {
        String userId = ulid.nextULID();
        UserMessage userMessage = new UserMessage(userId, message, System.currentTimeMillis());
        mMessageListAdapter.addFirst(userMessage);

        mMessageService = (MessageService) NetworkUtils.fetch(MessageService.class);
        mCall = mMessageService.getHistory(message);
        mCall.enqueue(mCallback);

        displayTextViewNewMessage();
    }

    private void displayTextViewNewMessage() {
        int firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
        if (firstVisibleItemPosition != 0) {
            mTextViewNewMessage.setVisibility(View.VISIBLE);
        } else {
            mTextViewNewMessage.setVisibility(View.GONE);
        }
    }

    private List<BaseMessage> generateBaseMessageList(int count) {
        List<BaseMessage> baseMessageList = new ArrayList<>();

        for (int index = 0; index < count; index++) {
            ULID.Value userIdValue = ulid.nextValue();
            String userId = userIdValue.toString();
            baseMessageList.add(new UserMessage(userId, "User Hello World " + index, System.currentTimeMillis()));

            ULID.Value botIdValue = ulid.nextValue();
            String botId = botIdValue.toString();
            baseMessageList.add(new BotMessage(botId, "Bot Hello world " + index, System.currentTimeMillis()));
        }

        return baseMessageList;
    }
}