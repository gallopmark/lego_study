package com.haoyu.app.utils;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.haoyu.app.base.GdeiApplication;
import com.haoyu.app.entity.LoginResult;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;


/**
 * Created by xiaoma on 2016/7/10.
 */
public class OkHttpClientManager {
    public final static int CONNECT_TIMEOUT = 60;
    public final static int READ_TIMEOUT = 120;
    public final static int WRITE_TIMEOUT = 120;
    private static volatile OkHttpClientManager mInstance;
    private OkHttpClient mOkHttpClient;
    private Gson mGson;

    public class CookieJarManager implements CookieJar {
        private final ArrayMap<String, List<Cookie>> cookieStore = new ArrayMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
    }

    private OkHttpClientManager() {
        CookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(GdeiApplication.getInstance()));
        mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
                .cookieJar(cookieJar)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("Accept-Encoding", "gzip")
                                .addHeader("Referer", Constants.REFERER)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();
        mGson = new Gson();
    }

    public static OkHttpClientManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpClientManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpClientManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 同步的Get请求
     *
     * @param url
     * @return Response
     */
    private Response _getAsyn(Context context, String url) throws Exception {
        return _getResponse(context, url);
    }

    /**
     * 同步的Get请求
     *
     * @param url
     * @return 字符串
     */
    private String _getAsString(Context context, String url) throws Exception {
        Response response = _getAsyn(context, url);
        String json = response.body().string();
        if ((json.contains("\"responseCode\":\"02\"") || json.contains("\"responseMsg\":\"no session\"")) && login(context))
            return _getAsString(context, url);
        return json;
    }

    /**
     * 异步的get请求
     *
     * @param url
     * @param callback
     */
    private Disposable _getAsyn(Context context, String url, final ResultCallback callback) {
        Request request = new Request.Builder().url(url).tag(context).build();
        return deliveryResult(context, callback, request);
    }

    /**
     * 同步的Post请求
     *
     * @param url
     * @param params post的参数
     * @return
     */
    private Response _post(Context context, String url, Param... params) throws Exception {
        return _postResonse(context, url, params);
    }

    private Response _post(Context context, String url, Map<String, String> params) throws Exception {
        return _postResonse(context, url, params);
    }

    /**
     * 同步的Post请求
     * 返回json字符串
     *
     * @param url
     * @param params post的参数
     * @return 字符串
     */
    private String _postAsString(Context context, String url, Param... params) throws Exception {
        Response response = _post(context, url, params);
        String json = response.body().string();
        if ((json.contains("\"responseCode\":\"02\"") || json.contains("\"responseMsg\":\"no session\"")) && login(context))
            return _postAsString(context, url, params);
        return json;
    }


    /**
     * 同步的Post请求
     * 返回json字符串
     *
     * @param url
     * @param params post的参数
     * @return 字符串
     */
    private String _postAsString(Context context, String url, Map<String, String> params) throws Exception {
        Response response = _post(context, url, params);
        String json = response.body().string();
        if ((json.contains("\"responseCode\":\"02\"") || json.contains("\"responseMsg\":\"no session\"")) && login(context))
            return _postAsString(context, url, params);
        return json;
    }

    /**
     * 异步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    private Disposable _postAsyn(Context context, String url, final ResultCallback callback,
                                 Param... params) {
        Request request = buildPostRequest(context, url, params);
        return deliveryResult(context, callback, request);
    }

    /**
     * 异步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    private Disposable _postAsyn(Context context, String url, final ResultCallback callback,
                                 Map<String, String> params) {
        Param[] paramsArr = map2Params(params);
        Request request = buildPostRequest(context, url, paramsArr);
        return deliveryResult(context, callback, request);
    }

    /**
     * 同步基于post的文件上传
     * 返回json字符串
     *
     * @return
     */
    private String _postFileAsString(Context context, String url, File file, String fileKey, ProgressListener progressListener) throws Exception {
        Response response = _postFileResponse(context, url, new File[]{file}, new String[]{fileKey}, progressListener);
        String json = response.body().string();
        if ((json.contains("\"responseCode\":\"02\"") || json.contains("\"responseMsg\":\"no session\"")) && login(context))
            return _postFileAsString(context, url, file, fileKey, progressListener);
        return json;
    }

    /**
     * 同步基于post的文件上传
     * 返回json字符串
     *
     * @return
     */
    private String _postFileAsString(Context context, String url, File file, String fileKey, ProgressListener progressListener, Param... params) throws Exception {
        Response response = _postFileResponse(context, url, new File[]{file}, new String[]{fileKey}, progressListener, params);
        String json = response.body().string();
        if ((json.contains("\"responseCode\":\"02\"") || json.contains("\"responseMsg\":\"no session\"")) && login(context))
            return _postFileAsString(context, url, file, fileKey, progressListener, params);
        return json;
    }

    private String _postFileAsString(Context context, String url, File[] files, String[] fileKeys, ProgressListener progressListener, Param... params) throws Exception {
        Response response = _postFileResponse(context, url, files, fileKeys, progressListener, params);
        String json = response.body().string();
        if ((json.contains("\"responseCode\":\"02\"") || json.contains("\"responseMsg\":\"no session\"")) && login(context))
            return _postFileAsString(context, url, files, fileKeys, progressListener, params);
        return json;
    }

    /**
     * 异步基于post的文件上传
     *
     * @param url
     * @param callback
     * @param files
     * @param fileKeys
     * @throws IOException
     */
    private Disposable _postAsyn(Context context, String url, ResultCallback callback, File[] files,
                                 String[] fileKeys, ProgressListener progressListener, Param... params) throws Exception {
        Request request = buildMultipartFormRequest(context, url, files, fileKeys, progressListener, params);
        return deliveryResult(context, callback, request);
    }

    /**
     * 异步基于post的文件上传，单文件不带参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @throws IOException
     */
    private void _postAsyn(Context context, String url, ResultCallback callback, File file,
                           String fileKey, ProgressListener progressListener) {
        Request request = buildMultipartFormRequest(context, url, new File[]{file}, new String[]{fileKey}, progressListener, null);
        deliveryResult(context, callback, request);
    }

    /**
     * 异步基于post的文件上传，单文件且携带其他form参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @param params
     * @throws IOException
     */
    private Disposable _postAsyn(Context context, String url, ResultCallback callback, File file,
                                 String fileKey, ProgressListener progressListener, Param... params) {
        Request request = buildMultipartFormRequest(context, url, new File[]{file}, new String[]{fileKey}, progressListener, params);
        return deliveryResult(context, callback, request);
    }

    /**
     * 异步Put请求
     *
     * @param url
     * @param callback
     * @param params
     */
    private void _putAsyn(Context context, String url, final ResultCallback callback,
                          Map<String, String> params) {
        Param[] paramsArr = map2Params(params);
        Request request = buildPutRequest(context, url, paramsArr);
        deliveryResult(context, callback, request);
    }

    private void _deleteAsyn(Context context, String url, final ResultCallback callback) {
        final Request request = new Request.Builder().url(url).delete().build();
        deliveryResult(context, callback, request);
    }

    private void _deleteAsyn(Context context, String url, ResultCallback callback, Map<String, String> params) {
        Param[] paramsArr = map2Params(params);
        Request request = buildDeleteRequest(url, paramsArr);
        deliveryResult(context, callback, request);
    }

    // *************对外公布的方法************

    public static String getAsString(Context context, String url) throws Exception {
        return getInstance()._getAsString(context, url);
    }

    public static Disposable getAsyn(Context context, String url, ResultCallback callback) {
        return getInstance()._getAsyn(context, url, callback);
    }

    public static String postAsString(Context context, String url, Param... params) throws Exception {
        return getInstance()._postAsString(context, url, params);
    }

    public static String postAsString(Context context, String url, Map<String, String> params) throws Exception {
        return getInstance()._postAsString(context, url, params);
    }

    public static Disposable postAsyn(Context context, String url, final ResultCallback callback,
                                      Param... params) {
        return getInstance()._postAsyn(context, url, callback, params);
    }

    public static Disposable postAsyn(Context context, String url, final ResultCallback callback,
                                      Map<String, String> params) {
        return getInstance()._postAsyn(context, url, callback, params);
    }

    public static String post(Context context, String url, File[] files, String[] fileKeys, ProgressListener progressListener,
                              Param... params) throws Exception {
        return getInstance()._postFileAsString(context, url, files, fileKeys, progressListener, params);
    }

    public static String post(Context context, String url, File file, String fileKey, ProgressListener progressListener)
            throws Exception {
        return getInstance()._postFileAsString(context, url, file, fileKey, progressListener);
    }

    public static String post(Context context, String url, File file, String fileKey, ProgressListener progressListener,
                              Param... params) throws Exception {
        return getInstance()._postFileAsString(context, url, file, fileKey, progressListener, params);
    }

    public static Disposable postAsyn(Context context, String url, ResultCallback callback,
                                      File[] files, String[] fileKeys, ProgressListener progressListener, Param... params)
            throws Exception {
        return getInstance()._postAsyn(context, url, callback, files, fileKeys, progressListener, params);
    }

    public static Disposable postAsyn(Context context, String url, ResultCallback callback, File file,
                                      String fileKey, ProgressListener progressListener, Param... params) {
        return getInstance()._postAsyn(context, url, callback, file, fileKey, progressListener, params);
    }

    public static void putAsyn(Context context, String url, final ResultCallback callback, Map<String, String> params) {
        getInstance()._putAsyn(context, url, callback, params);
    }

    public static void deleteAsyn(Context context, String url, final ResultCallback callback) {
        getInstance()._deleteAsyn(context, url, callback);
    }

    public static void deleteAsyn(Context context, String url, final ResultCallback callback,
                                  Map<String, String> params) {
        getInstance()._deleteAsyn(context, url, callback, params);
    }

    // ****************************

    private Request buildMultipartFormRequest(Context context, String url, File[] files,
                                              String[] fileKeys, ProgressListener progressListener, Param[] params) {
        params = validateParam(params);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        for (Param param : params) {
            builder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\""
                            + param.key + "\""),
                    RequestBody.create(null, param.value));
            builder.addFormDataPart(param.key, param.value);
        }
        if (files != null) {
            RequestBody fileBody;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                fileBody = RequestBody.create(
                        MediaType.parse(guessMimeType(fileName)), file);
                builder.addFormDataPart("file", file.getName(), new ProgressRequestBody(fileBody, progressListener, file));
//                // TODO 根据文件名设置contentType
//                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\""
//                        + fileKeys[i] + "\"; filename=\"" + fileName
//                        + "\""), fileBody);
//                builder.addFormDataPart("file", file.getName(), createCustomRequestBody(MultipartBody.FORM, file, progressListener));
//                builder.addPart(fileBody);
            }
        }
        RequestBody requestBody = builder.build();
        return new Request.Builder().url(url).tag(context).post(requestBody).build();
    }

    /**
     * 包装的请求体，处理进度
     * User:lizhangqu(513163535@qq.com)
     * Date:2015-09-02
     * Time: 17:15
     */
    public class ProgressRequestBody extends RequestBody {
        //实际的待包装请求体
        private final RequestBody requestBody;
        //进度回调接口
        private final ProgressListener progressListener;
        //包装完成的BufferedSink
        private BufferedSink bufferedSink;
        private File file;

        /**
         * 构造函数，赋值
         *
         * @param requestBody      待包装的请求体
         * @param progressListener 回调接口
         */
        public ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener, File file) {
            this.requestBody = requestBody;
            this.progressListener = progressListener;
            this.file = file;
        }

        /**
         * 重写调用实际的响应体的contentType
         *
         * @return MediaType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         *
         * @return contentLength
         * @throws IOException 异常
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         *
         * @param sink BufferedSink
         * @throws IOException 异常
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                //包装
                bufferedSink = Okio.buffer(sink(sink));
            }
            //写入
            requestBody.writeTo(bufferedSink);
            //必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush();

        }

        /**
         * 写入，回调进度接口
         *
         * @param sink Sink
         * @return Sink
         */
        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                //当前写入字节数
                long bytesWritten = 0L;
                //总字节长度，避免多次调用contentLength()方法
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        //获得contentLength的值，后续不再调用
                        contentLength = contentLength();
                    }
                    //增加当前写入的字节数
                    bytesWritten += byteCount;
                    //回调
                    if (progressListener != null) {
                        progressListener.onProgress(contentLength, contentLength - bytesWritten, bytesWritten == contentLength, file);
                    }
                }
            };
        }
    }

    public interface ProgressListener {
        void onProgress(long totalBytes, long remainingBytes, boolean done, File file);
    }

    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    private Param[] validateParam(Param[] params) {
        if (params == null)
            return new Param[0];
        else
            return params;
    }


    private Param[] map2Params(Map<String, String> params) {
        if (params == null)
            return new Param[0];
        int size = params.size();
        Param[] res = new Param[size];
        Set<Map.Entry<String, String>> entries = params.entrySet();
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            res[i++] = new Param(entry.getKey(), entry.getValue());
        }
        return res;
    }

    private Disposable deliveryResult(final Context context, final ResultCallback callback, final Request request) {
        if (callback == null) {
            return null;
        }
        final ResultCallback resCallBack = callback;
        resCallBack.onBefore(request);
        return Flowable.just(request).map(new Function<Request, String>() {
            @Override
            public String apply(Request request) throws Exception {
                Response response = mOkHttpClient.newCall(request).execute();
                String json = response.body().string();
                if ((json.contains("\"responseCode\":\"02\"") || json.contains("\"responseMsg\":\"no session\"")) && login(context)) {
                    return apply(request);
                }
                return json;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String json) throws Exception {
                        Log.e("json", json);
                        if (resCallBack.mType == String.class) {
                            resCallBack.onResponse(json);
                        } else {
                            Object obj = mGson.fromJson(json, resCallBack.mType);
                            resCallBack.onResponse(obj);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        resCallBack.onError(request, (Exception) e);
                    }
                });
    }

    private Request buildPostRequest(Context context, String url, Param[] params) {
        if (params == null) {
            params = new Param[0];
        }
        FormBody.Builder formEncodingBuilder = new FormBody.Builder();
        for (Param param : params) {
            if (param.value != null) {
                formEncodingBuilder.add(param.key, param.value);
            }
        }
        RequestBody requestBody = formEncodingBuilder.build();
        return new Request.Builder().url(url).tag(context).post(requestBody).build();
    }

    private Request buildPutRequest(Context context, String url, Param[] params) {
        if (params == null) {
            params = new Param[0];
        }
        FormBody.Builder formEncodingBuilder = new FormBody.Builder();
        for (Param param : params) {
            if (param.value != null) {
                formEncodingBuilder.add(param.key, param.value);
            }
        }
        RequestBody requestBody = formEncodingBuilder.build();
        return new Request.Builder().url(url).tag(context).put(requestBody).build();
    }

    private Request buildDeleteRequest(String url, Param[] params) {
        if (params == null) {
            params = new Param[0];
        }
        FormBody.Builder formEncodingBuilder = new FormBody.Builder();
        for (Param param : params) {
            if (param.value != null) {
                formEncodingBuilder.add(param.key, param.value);
            }
        }
        RequestBody requestBody = formEncodingBuilder.build();
        return new Request.Builder().url(url).delete(requestBody).build();
    }

    public static abstract class ResultCallback<T> {
        Type mType;

        public ResultCallback() {
            try {
                mType = getSuperclassTypeParameter(getClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterized = (ParameterizedType) superclass;
            return $Gson$Types.canonicalize(parameterized
                    .getActualTypeArguments()[0]);
        }

        public void onBefore(Request request) {
        }

        public abstract void onError(Request request, Exception e);

        public abstract void onResponse(T response);
    }

    public static class Param {
        public Param() {
        }

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key;
        public String value;
    }

    /**
     * 取消当前context的所有请求
     *
     * @param context
     */
    public void cancel(Context context) {
        if (mOkHttpClient != null) {
            for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
                if (call.request().tag().equals(context))
                    call.cancel();
            }
            for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
                if (call.request().tag().equals(context))
                    call.cancel();
            }
        }
    }

    public boolean login(Context context) throws Exception {
        final String url = Constants.LOGIN_URL;
        Map<String, String> map = new HashMap<>();
        String username = SharePreferenceHelper.getAccount(context);
        String password = SharePreferenceHelper.getPassWord(context);
        map.put("username", username);
        map.put("password", password);
        Response response = _postResonse(context, url, map);
        String tgtUrl = response.body().string();
        response.close();
        map.clear();
        map.put("service", Constants.OUTRT_NET + "/shiro-cas");
        response = _postResonse(context, tgtUrl, map);
        String stStr = response.body().string();
        response.close();
        String logUrl = Constants.OUTRT_NET + "/shiro-cas" + "?ticket=" + stStr;
        response = _getResponse(context, logUrl);
        return response.isSuccessful();
    }

    /**
     * 登录页面登录
     *
     * @param context
     * @param url
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public LoginResult login(Context context, String url, String username, String password) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        Response response = _postResonse(context, url, map);
        String tgtUrl = response.body().string();
        response.close();
        map.clear();
        map.put("service", Constants.OUTRT_NET + "/shiro-cas");
        response = _postResonse(context, tgtUrl, map);
        String stStr = response.body().string();
        response.close();
        String logUrl = Constants.OUTRT_NET + "/shiro-cas" + "?ticket=" + stStr;
        response = _getResponse(context, logUrl);
        if (response.isSuccessful()) {
            response.close();
            response = _getResponse(context, Constants.OUTRT_NET + "/shiro-cas");
            String responseStr = response.body().string();
            Gson gson = new Gson();
            LoginResult result = gson.fromJson(responseStr, LoginResult.class);
            return result;
        }
        return null;
    }

    /**
     * 扫一扫登录
     *
     * @param context
     * @param url
     * @param qtId
     * @param service
     * @return
     * @throws Exception
     */
    public boolean scanLogin(Context context, String url, String qtId, String service) throws Exception {
        String username = SharePreferenceHelper.getAccount(context);
        String password = SharePreferenceHelper.getPassWord(context);
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        Response response = _postResonse(context, url, map);
        String tgtUrl = response.body().string();
        String ST = "";
        if (service != null && service.length() > 0) {
            response.close();
            map.clear();
            map.put("service", service);
            response = _postResonse(context, tgtUrl, map);
            ST = response.body().string();
            response.close();
        }
        tgtUrl = tgtUrl.substring(tgtUrl.lastIndexOf("/") + 1, tgtUrl.length());
        String logUrl = Constants.LOGIN_SERVER + "/qrcode/appLogin?tgt=" + tgtUrl + "&st=" + ST + "&qt=" + qtId;
        response = _getResponse(context, logUrl);
        return response.isSuccessful();
    }

    private Response _postResonse(Context context, String url, Map<String, String> params) throws Exception {
        Param[] paramsArr = map2Params(params);
        Request request = buildPostRequest(context, url, paramsArr);
        Response response = mOkHttpClient.newCall(request).execute();
        return response;
    }

    private Response _postResonse(Context context, String url, Param... params) throws Exception {
        Request request = buildPostRequest(context, url, params);
        Response response = mOkHttpClient.newCall(request).execute();
        return response;
    }

    private Response _getResponse(Context context, String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .tag(context)
                .build();
        Response response = mOkHttpClient.newCall(request).execute();
        return response;
    }

    private Response _postFileResponse(Context context, String url, File[] files, String[] fileKeys, ProgressListener progressListener, Param... params) throws Exception {
        Request request = buildMultipartFormRequest(context, url, files, fileKeys, progressListener, params);
        Response response = mOkHttpClient.newCall(request).execute();
        return response;
    }
}
