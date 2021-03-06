package com.example.smart_medical_app

import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

class KeepAliveService : JobService() {
    private lateinit var LocalServiceUntils:ServiceUtils
    private lateinit var RemoteServiceUntils:ServiceUtils
    private lateinit var MQTTServiceUntils:ServiceUtils
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.e("JAMES","JobService onStartJob")
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
            startJob(this)
        }
        LocalServiceUntils= ServiceUtils(this,MyLocalService::class.java.name)
        val isLocalServiceRunning: Boolean = LocalServiceUntils.isServiceRunning()
        if (!isLocalServiceRunning) {
            startService(Intent(this, MyLocalService::class.java))
        }
        RemoteServiceUntils= ServiceUtils(this,MyRemoteService::class.java.name)
        val isRemoteServiceRunning: Boolean = RemoteServiceUntils.isServiceRunning()
        if (!isRemoteServiceRunning) {
            startService(Intent(this, MyRemoteService::class.java))
        }
        MQTTServiceUntils= ServiceUtils(this,MyMQTTService::class.java.name)
        val isMQTTServiceRunning:Boolean=MQTTServiceUntils.isServiceRunning()
        Log.e("JAMES","MQTTService:$isMQTTServiceRunning")
        if(!isMQTTServiceRunning){
            startService(Intent(this,MyMQTTService::class.java))
        }
        return false
    }
    companion object{
        fun startJob(context: Context) {
            val jobScheduler: JobScheduler =context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val jobInfoBuilder = JobInfo.Builder(
                10,
                ComponentName(context.packageName, KeepAliveService::class.java.name)
            ).setPersisted(true)
            // 7.0 ???????????????, ???????????? 5000 ????????????????????????
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                jobInfoBuilder.setPeriodic(5_000);

            }else{
                // 7.0 ??????????????? , ???????????? 5 ?????????
                // ????????????????????? JobInfo.getMinLatencyMillis ????????????????????????
                jobInfoBuilder.setMinimumLatency(5_000)
            }
            jobScheduler.schedule(jobInfoBuilder.build())
        }
    }
    override fun onStopJob(params: JobParameters?): Boolean {
        Log.i("JAMES", "JobService onStopJob ??????");
        return false
    }
}