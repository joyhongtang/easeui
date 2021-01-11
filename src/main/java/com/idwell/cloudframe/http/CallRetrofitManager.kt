package com.idwell.cloudframe.http

import android.util.Log
import com.hyphenate.chatuidemo.Constant
import com.idwell.cloudframe.util.Sha256Util
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object CallRetrofitManager {

    //    private const val URL = "https://well.bsimb.cn/;
    private const val URL = "http://well.bsimb.cn/apiv1/"

    //    https://well.bsimb.cn/
    const val WEATHER_ICON_URL = "http://openweathermap.org/img/w/"

    private var mRetrofit: Retrofit
    private var mOkHttpClient: OkHttpClient
    //private lateinit var progressListener: ProgressListener

    init {
        //SSL证书
        val x509TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {

            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(x509TrustManager), SecureRandom())
        //Log拦截器
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        mOkHttpClient =
            OkHttpClient.Builder().sslSocketFactory(sslContext.socketFactory, x509TrustManager)
                .addInterceptor { chain ->
                    val timestamp = System.currentTimeMillis().toString()
                    var mBuilder = chain.request().newBuilder()
                    val request = mBuilder
//                        .addHeader("serial_number", Device.snnumber)
//                        .addHeader("mac_address", DeviceUtils.getMacAddress())
                        .build()
                    var URLInfo = request.url().url()
                    var pathPrefix = request.url().uri().path
                    //是激活或者登陆接口
                    if (pathPrefix.contains("device/device_active") || pathPrefix.contains("device/signin")) {
                        val headerParms = TreeMap<String, String>()
                        headerParms.put("timestamp", timestamp)
                        val sb:StringBuffer = StringBuffer()
                        val keySets = headerParms.entries
                        for(setItem in keySets){
                            sb.append(setItem.key)
                            sb.append(setItem.value)
                        }

                        val sign = Constant.SNNUMBER + sb.toString() +Constant.SNNUMBER
                        val md5Sign = Sha256Util.md5Encode(sign)
                        mBuilder.addHeader("timestamp",timestamp)
                        mBuilder.addHeader("sign",md5Sign)
                    }else{
                        val headerParms:TreeMap<String,String> = TreeMap<String, String>()
                        headerParms.put("timestamp", timestamp)
                        headerParms.put("device_id", Constant.ID.toString())
                        val sb:StringBuffer = StringBuffer()
                        val keySets = headerParms.entries
                        for(setItem in keySets){
                            sb.append(setItem.key)
                            sb.append(setItem.value)
                        }
                        val sign = Constant.GENERATE_SECRET + sb.toString() +Constant.GENERATE_SECRET
                        val md5Sign = Sha256Util.md5Encode(sign)
                        mBuilder.addHeader("timestamp",timestamp)
                        mBuilder.addHeader("sign",md5Sign)
                        mBuilder.addHeader("device_id",Constant.ID.toString())
                        Log.d("OkHttp head ","timestamp = "+timestamp+" sign = "+md5Sign+" device_id = "+Constant.ID)

                    }
                    chain.proceed(request)
                }.addNetworkInterceptor(httpLoggingInterceptor)
                .readTimeout(15000, TimeUnit.MILLISECONDS)
                .writeTimeout(15000, TimeUnit.MILLISECONDS)
                .connectTimeout(15000, TimeUnit.MILLISECONDS)
                //超时时间设置，默认15秒
                .readTimeout(15, TimeUnit.SECONDS)      //全局的读取超时时间
                .writeTimeout(15, TimeUnit.SECONDS)     //全局的写入超时时间
                .connectTimeout(15, TimeUnit.SECONDS)   //全局的连接超时时间
                .build()
        mRetrofit = Retrofit.Builder()
            .client(mOkHttpClient)
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        //ve
//        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
//        val builder = OkHttpClient.Builder().addInterceptor { chain ->
//            val timestamp =  System.currentTimeMillis()
//            val request = chain.request().newBuilder()
//                .addHeader("api_auth", Sha256Util.getSHA256(Device.snnumber+timestamp))
//                .addHeader("api_timestamp",timestamp.toString())
//                .addHeader("api_id",Device.id.toString())
//                .build()
//            chain.proceed(request)
//        }.addNetworkInterceptor(httpLoggingInterceptor).readTimeout(15000, TimeUnit.MILLISECONDS)
//            .writeTimeout(15000, TimeUnit.MILLISECONDS)
//            .connectTimeout(15000, TimeUnit.MILLISECONDS)
//        mRetrofit = Retrofit.Builder().client(builder.build()).baseUrl(URL)
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()

    }

    /*private val progressRetro: Retrofit
        get() {
            val builder = OkHttpClient.Builder()
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
            builder.addNetworkInterceptor(loggingInterceptor)
            //进度
            builder.addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                originalResponse.newBuilder()
                    .body(originalResponse.body()?.let { ProgressResponseBody(it, progressListener) })
                    .build()
            }
            //全局的读取超时时间，默认15秒
            builder.readTimeout(15, TimeUnit.SECONDS)
            //全局的写入超时时间
            builder.writeTimeout(15, TimeUnit.SECONDS)
            //全局的连接超时时间
            builder.connectTimeout(15, TimeUnit.SECONDS)
            val okHttpClient = builder.build()
            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }*/

    fun <T> getService(service: Class<T>): T {
        return mRetrofit.create(service)
    }

    /*fun <T> getProgressService(progressListener: ProgressListener, service: Class<T>): T {
        this.progressListener = progressListener
        return progressRetro.create(service)
    }*/
}