package id.jefrydco.botcoll.message;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
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
import id.jefrydco.botcoll.auth.LoginActivity;
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

    String mMessage;

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

                    StringBuilder subjectPIC = new StringBuilder();
                    subjectPIC.append("Taraaa!!! Berikut daftar PJ kelas kamu:\n");

                    StringBuilder subjectList = new StringBuilder();
                    subjectList.append("Yuhuu!!! Berikut daftar matkul kelas kamu:\n");

                    for (int index = 0; index < historyArray.length(); index++) {
                        JSONObject historyArrayItem = historyArray.getJSONObject(index);

                        ULID.Value botIdValue = ulid.nextValue();
                        String botId = botIdValue.toString();

                        switch (mMessage) {
                            case "jadwal kuliah hari senin":
                                String dayCollege = historyArrayItem.getString("hari");
                                int subjectCollege = historyArrayItem.getInt("matkul");
                                String roomCollege = historyArrayItem.getString("kelas");
                                String dateCollege = historyArrayItem.getString("tanggal");

                                mMessageListAdapter.addFirst(new BotMessage(
                                        botId,
                                        "Jadwal matkul hari " + dayCollege + " tanggal " + dateCollege + " di kelas " + roomCollege + " adalah " + subjectCollege,
                                        System.currentTimeMillis()));
                                break;
                            case "jadwal ujian hari senin":
                                String dateTest = historyArrayItem.getString("tanggal");
                                String subjectTest = historyArrayItem.getString("id_jadkul");
                                String roomTest = historyArrayItem.getString("kelas");
                                mMessageListAdapter.addFirst(new BotMessage(
                                        botId,
                                        "Jadwal ujian tanggal " + dateTest + " di kelas " + roomTest + " adalah " + subjectTest,
                                        System.currentTimeMillis()
                                ));
                                break;
                            case "daftar pj":
                            case "pj":
                                String picName = historyArrayItem.getString("nama_pj");
                                String picSubject = historyArrayItem.getString("id_matkul");
                                subjectPIC.append(index + 1).append(". ").append(picName);
                                if (!(index == historyArray.length() - 1)) {
                                    subjectPIC.append("\n");
                                }
                                break;
                            case "daftar matkul":
                            case "matkul":
                                String subjectName = historyArrayItem.getString("matkul");
                                subjectList.append(index + 1).append(". ").append(subjectName);
                                if (!(index == historyArray.length() - 1)) {
                                    subjectList.append("\n");
                                }
                                break;
                        }
                    }

                    if (!subjectPIC.toString().equals("Taraaa!!! Berikut daftar PJ kelas kamu:\n")) {
                        mMessageListAdapter.addFirst(new BotMessage(
                                ulid.nextULID(),
                                subjectPIC.toString(),
                                System.currentTimeMillis()
                        ));
                    }

                    if (!subjectList.toString().equals("Yuhuu!!! Berikut daftar matkul kelas kamu:\n")) {
                        mMessageListAdapter.addFirst(new BotMessage(
                                ulid.nextULID(),
                                subjectList.toString(),
                                System.currentTimeMillis()
                        ));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                switch (mMessage) {
                    case "keyword":
                    case "daftar keyword":
                    case "daftar kata kunci":
                        mMessageListAdapter.addFirst(appendKeywordMessage());
                        break;
                    case "hello":
                    case "hai":
                    case "helo":
                        mMessageListAdapter.addFirst(new BotMessage(
                                ulid.nextULID(),
                                "ya?",
                                System.currentTimeMillis()
                        ));
                        break;
                    case "lagi apa?":
                    case "hai lagi apa?":
                        mMessageListAdapter.addFirst(new BotMessage(
                                ulid.nextULID(),
                                "kepo deh...",
                                System.currentTimeMillis()
                        ));
                        break;
                    default:
                        mMessageListAdapter.addFirst(new BotMessage(
                                ulid.nextULID(),
                                "Monmap yaaa aing ndak ngerti maksud kamu :(",
                                System.currentTimeMillis()
                        ));
                        break;
                }
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

        Intent intentToLoginActivityFromMessageActivity = new Intent(this, LoginActivity.class);
        startActivity(intentToLoginActivityFromMessageActivity);

        setupAdapter();
        setupLayoutManager();
        setupRecyclerView();
        setupChatbox();
        setupNewMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_logout:
                Intent intentToLoginActivityFromMessageActivity = new Intent(this, LoginActivity.class);
                startActivity(intentToLoginActivityFromMessageActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupAdapter() {
//        List<BaseMessage> baseMessageList = generateBaseMessageList(DATA_SIZE);
        List<BaseMessage> baseMessageList = new ArrayList<>();
        baseMessageList.add(appendKeywordMessage());

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
            mMessage = mEditTextChatboxInput.getText().toString().toLowerCase();
            if (mMessage.length() > 0) {
                sendUserMessage(mMessage);
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
        String keyword = "";

        switch (message) {
            case "jadwal kuliah hari senin":
                keyword = "jadwal_kuliah hari senin";
                break;
            case "jadwal ujian hari senin":
                keyword = "jadwal_ujian tanggal 1/06/2018";
                break;
            case "daftar pj":
            case "pj":
                keyword = "pj";
                break;
            case "daftar matkul":
            case "matkul":
                keyword = "daftar_matkul";
                break;

        }

        UserMessage userMessage = new UserMessage(userId, message, System.currentTimeMillis());
        mMessageListAdapter.addFirst(userMessage);

        mMessageService = (MessageService) NetworkUtils.fetch(MessageService.class);
        mCall = mMessageService.getHistory(keyword);
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

    private BaseMessage appendKeywordMessage() {
        return new BotMessage(
                ulid.nextULID(),
                "Halooo!!!\n" +
                        "Selamat datang di BotColl, kamu dapat melihat jadwal kuliah dengan mengetikkan keyword berikut:\n" +
                        "1. Jadwal kuliah hari <hari>\n" +
                        "2. Jadwal ujian hari <hari>\n" +
                        "3. Daftar pj\n" +
                        "4. Daftar matkul" +
                        "\n\n" +
                        "Kuy kuy langsung ajah cobain BotColl dan rasakan sensaninya...",
                System.currentTimeMillis()
            );
        }
}