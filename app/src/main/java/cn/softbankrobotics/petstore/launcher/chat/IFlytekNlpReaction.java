package cn.softbankrobotics.petstore.launcher.chat;

import android.content.Context;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.aldebaran.qi.sdk.util.IOUtils;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * The ChatbotReaction of IFlytek.
 */
public class IFlytekNlpReaction extends BaseChatbotReaction {
    private static String TAG = IFlytekNlpReaction.class.getSimpleName();
    private Context mContext;
    private int mAIUIState = AIUIConstant.STATE_IDLE;
    private String question;
    /**
     * TTS是否开启需要在讯飞AIUI云端同步配置
     */
    private boolean useIFlytekTTS = true;

    public IFlytekNlpReaction(QiContext context, String question) {
        super(context);
        this.mContext = context;
        this.question = question;
    }

    public AIUIAgent createAgent(MyAIUIListener mAIUIListener) {
        Log.i(TAG, "create aiui agent");
        try {
            String params = IOUtils.fromAsset(mContext, "cfg/aiui_phone.cfg");
            params = params.replace("\n", "").replace("\t", "").replace(" ", "");
            AIUIAgent aIUIAgent = AIUIAgent.createAgent(mContext, params, mAIUIListener);
            return aIUIAgent;
        } catch (Exception e) {
            Log.e(TAG,"Error during createAgent " + e);
            return null;
        }

    }

    @Override
    public void runWith(SpeechEngine speechEngine) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        MyAIUIListener myAIUIListener = new MyAIUIListener();
        myAIUIListener.setCountDownLatch(countDownLatch);
        AIUIAgent aiuiAgent = createAgent(myAIUIListener);

        sendNlpMessage(aiuiAgent);
        try {
            // 当前线程阻塞，直到其他线程执行完毕回到当前线程。
            countDownLatch.await();
        } catch (InterruptedException e) {

        }
        if (!useIFlytekTTS) {
            Say say = SayBuilder.with(speechEngine)
                    .withText(myAIUIListener.getAnswer())
                    .build();

            Future fSay = say.async().run();

            try {
                fSay.get();
            } catch (ExecutionException e) {
                Log.e(TAG, "Error during Say", e);
            } catch (CancellationException e) {
                Log.i(TAG, "Interruption during Say");
            }
        }
        aiuiAgent.destroy();
    }

    private void sendNlpMessage(AIUIAgent mAIUIAgent) {
        if (AIUIConstant.STATE_WORKING != mAIUIState) {
            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        Log.i(TAG, "start text nlp");
        try {
            String params = "data_type=text,tag=text-tag";
            byte[] textData = question.getBytes("utf-8");

            AIUIMessage write = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, textData);
            mAIUIAgent.sendMessage(write);


        } catch (UnsupportedEncodingException e) {
            Log.e(TAG,"Error during sendNlpMessage" + e);
        }
    }

    @Override
    public void stop() {
    }

    private class MyAIUIListener implements AIUIListener {
        private String answer;
        private volatile CountDownLatch countDownLatch;
        private JSONObject resultEvent;

        public void setCountDownLatch(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        public String getAnswer() {
            return answer;
        }

        public JSONObject getResultEvent() {
            return resultEvent;
        }

        @Override
        public void onEvent(AIUIEvent event) {
            Log.i(TAG, "on event: " + event.eventType);

            switch (event.eventType) {
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                    break;

                case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                    if (countDownLatch.getCount() == 1) {
                        countDownLatch.countDown();
                    }
                    break;

                case AIUIConstant.EVENT_WAKEUP:
                    break;

                case AIUIConstant.EVENT_RESULT: {
                    try {
                        JSONObject data = new JSONObject(event.info).getJSONArray("data").getJSONObject(0);
                        String sub = data.getJSONObject("params").optString("sub");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if ("nlp".equals(sub)) {
                            if (content.has("cnt_id")) {
                                String cnt_id = content.getString("cnt_id");
                                JSONObject result = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));
                                resultEvent = result;
                                int rc = result.getJSONObject("intent").getInt("rc");
                                if (rc != 0) {
                                    Log.w(TAG, "nlp rc: " + rc);
                                    countDownLatch.countDown();
                                }

                                answer = result.getJSONObject("intent").getJSONObject("answer").getString("text");
                            }
                        }

                    } catch (Throwable e) {
                        Log.e(TAG,"Error during EVENT_RESULT" + e);
                    } finally {
                        if (!useIFlytekTTS) {
                            countDownLatch.countDown();
                        }
                    }
                    break;
                }

                case AIUIConstant.EVENT_ERROR: {
                    Log.e(TAG, "Event error: " + event.info);
                    countDownLatch.countDown();
                }
                break;

                case AIUIConstant.EVENT_VAD: {
                }
                break;

                case AIUIConstant.EVENT_START_RECORD: {
                }
                break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                }
                break;

                case AIUIConstant.EVENT_STATE: {

                }
                break;

                case AIUIConstant.EVENT_CMD_RETURN: {

                }

                case AIUIConstant.EVENT_TTS: {
                    switch (event.arg1) {
                        case AIUIConstant.TTS_SPEAK_BEGIN:
                            break;

                        case AIUIConstant.TTS_SPEAK_PROGRESS:
                            break;

                        case AIUIConstant.TTS_SPEAK_PAUSED:
                            break;

                        case AIUIConstant.TTS_SPEAK_RESUMED:
                            break;

                        case AIUIConstant.TTS_SPEAK_COMPLETED:
                            if (useIFlytekTTS) {
                                countDownLatch.countDown();
                            }
                            break;

                        default:
                            break;
                    }
                }
                break;

                default:
                    break;
            }
        }
    }
}