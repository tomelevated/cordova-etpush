package com.salesforce.etpush;

import android.app.Application;
import android.util.Log;
import android.content.pm.ApplicationInfo;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Set;
import java.util.ArrayList;

import com.exacttarget.etpushsdk.ETException;
import com.exacttarget.etpushsdk.ETPush;
import com.exacttarget.etpushsdk.ETPushConfig;
import com.exacttarget.etpushsdk.data.Attribute;
import com.exacttarget.etpushsdk.data.Message;
import com.exacttarget.etpushsdk.event.*;
import com.exacttarget.etpushsdk.util.EventBus;
import com.exacttarget.etpushsdk.ETLocationManager;
import com.exacttarget.etpushsdk.adapter.CloudPageListAdapter;

public class ETPushCordovaApplication extends Application {

	private static final String TAG = CONSTS.LOG_TAG;
	
	public static CDVCloudPageListAdapter cloudPageListAdapter;

	private String devEtAppId, devAccessToken, devGcmSenderId, prodEtAppId, prodAccessToken, prodGcmSenderId;
	private Boolean analyticsEnabled, piAnalyticsEnabled, locationEnabled, proximityEnabled, cloudPagesEnabled;

  /**
   * OnCreate for the Application. Init ETSDK with ETPush.readyAimFire
   */
  @Override
  public void onCreate() {
    super.onCreate();

    EventBus.getInstance().register(this);

		ETPushConfig pushConfig = null;
		try {

			//Get ETSDK configuration from secrets.xml resource
			devEtAppId = this.getResources().getString(this.getResources().getIdentifier("ETPUSH_DEV_APPID", "string", this.getPackageName()));
			if (devEtAppId == null || devEtAppId.isEmpty()) throw new RuntimeException("Unable to find ETPUSH_DEV_APPID in secrets.xml");	
			devAccessToken = this.getResources().getString(this.getResources().getIdentifier("ETPUSH_DEV_ACCESSTOKEN", "string", this.getPackageName()));
			if (devAccessToken == null || devAccessToken.isEmpty()) throw new RuntimeException("Unable to find ETPUSH_DEV_ACCESSTOKEN in secrets.xml");	
			devGcmSenderId = this.getResources().getString(this.getResources().getIdentifier("ETPUSH_DEV_GCMSENDERID", "string", this.getPackageName()));
			if (devGcmSenderId == null || devGcmSenderId.isEmpty()) throw new RuntimeException("Unable to find ETPUSH_DEV_GCMSENDERID in secrets.xml");	
			prodEtAppId = this.getResources().getString(this.getResources().getIdentifier("ETPUSH_PROD_APPID", "string", this.getPackageName()));
			if (prodEtAppId == null || prodEtAppId.isEmpty()) throw new RuntimeException("Unable to find ETPUSH_PROD_APPID in secrets.xml");	
			prodAccessToken = this.getResources().getString(this.getResources().getIdentifier("ETPUSH_PROD_ACCESSTOKEN", "string", this.getPackageName()));
			if (prodAccessToken == null || prodAccessToken.isEmpty()) throw new RuntimeException("Unable to find ETPUSH_PROD_ACCESSTOKEN in secrets.xml");	
			prodGcmSenderId = this.getResources().getString(this.getResources().getIdentifier("ETPUSH_PROD_GCMSENDERID", "string", this.getPackageName()));
			if (prodGcmSenderId == null || prodGcmSenderId.isEmpty()) throw new RuntimeException("Unable to find ETPUSH_PROD_GCMSENDERID in secrets.xml");	
			analyticsEnabled = this.getResources().getBoolean(this.getResources().getIdentifier("ETPUSH_ANALYTICS_ENABLED", "bool", this.getPackageName()));
			if (analyticsEnabled == null) throw new RuntimeException("Unable to find ETPUSH_ANALYTICS_ENABLED in secrets.xml");	
			piAnalyticsEnabled = this.getResources().getBoolean(this.getResources().getIdentifier("ETPUSH_WAMA_ENABLED", "bool", this.getPackageName()));
			if (piAnalyticsEnabled == null) throw new RuntimeException("Unable to find ETPUSH_WAMA_ENABLED in secrets.xml");	
			locationEnabled = this.getResources().getBoolean(this.getResources().getIdentifier("ETPUSH_LOCATION_ENABLED", "bool", this.getPackageName()));
			if (locationEnabled == null) throw new RuntimeException("Unable to find ETPUSH_LOCATION_ENABLED in secrets.xml");	
			proximityEnabled = this.getResources().getBoolean(this.getResources().getIdentifier("ETPUSH_PROXIMITY_ENABLED", "bool", this.getPackageName()));
			if (proximityEnabled == null) throw new RuntimeException("Unable to find ETPUSH_PROXIMITY_ENABLED in secrets.xml");	
			cloudPagesEnabled = this.getResources().getBoolean(this.getResources().getIdentifier("ETPUSH_CLOUDPAGES_ENABLED", "bool", this.getPackageName()));
			if (cloudPagesEnabled == null) throw new RuntimeException("Unable to find ETPUSH_CLOUDPAGES_ENABLED in secrets.xml");	


			String etAppId;  
			String accessToken;
			String gcmSenderId;
			boolean isDebuggable = (0 != (this.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
	        if (isDebuggable) {
	            ETPush.setLogLevel(Log.DEBUG);
	            etAppId = devEtAppId;
	            accessToken = devAccessToken;
	            gcmSenderId = devGcmSenderId;
	        }
	        else {
	            etAppId = prodEtAppId;
	            accessToken = prodAccessToken;
	            gcmSenderId = prodGcmSenderId;
	        }
	            
			//Build ETPushConfig with Options
			pushConfig = new ETPushConfig.Builder(this)
		    		.setEtAppId(etAppId)
		    		.setAccessToken(accessToken)
		    		.setGcmSenderId(gcmSenderId)
		    		.setAnalyticsEnabled(analyticsEnabled)
		    		.setPiAnalyticsEnabled(piAnalyticsEnabled)
		    		.setLocationEnabled(locationEnabled)   
		    		.setProximityEnabled(proximityEnabled) 
		    		.setCloudPagesEnabled(cloudPagesEnabled)
		    		.setLogLevel(Log.DEBUG)    
		    		.build();

		} catch (Exception e) {
	  	Log.e(TAG, e.getMessage(), e);			
		}

		try {	
			//Init ETSDK with ETPush.readyAimFire
			if (pushConfig != null) {
				ETPush.readyAimFire(pushConfig);
			} else {
				Log.e(TAG, "ETPushConfig is null");				
			}
		} catch (ETException e) {
	  	Log.e(TAG, e.getMessage(), e);
		}   

    cloudPageListAdapter = new CDVCloudPageListAdapter(getApplicationContext());      
  }

  /**
   * EventBus callback listening for a ReadyAimFireInitCompletedEvent.
   *
   * @param event the type of event we're listening for.
   */
	public void onEvent(ReadyAimFireInitCompletedEvent event) {
  	Log.i(TAG, "EventBus ReadyAimFireInitCompletedEvent: " + event.toString());

	   if (ETPush.getLogLevel() <= Log.DEBUG) {
	       Log.i(TAG, "ReadyAimFireInitCompletedEvent started.");
	   }
	        
	   if (event.isReadyAimFireReady()) {
	     // successful bootstrap with SDK  
	     Log.i(TAG, "Successfully bootstrapped readyAimFire"); 
	     try {
	     	ETPush.getInstance().setCloudPageRecipient(ETPushCordovaLandingPagePassthrough.class); 
	     	ETPush.getInstance().setOpenDirectRecipient(ETPushCordovaLandingPagePassthrough.class); 

	      if (locationEnabled) ETLocationManager.getInstance().startWatchingLocation(); //should start automatically
	      if (proximityEnabled) ETLocationManager.getInstance().startWatchingProximity(); //need to start manually
	     } catch (ETException e) {
          	Log.e(TAG, e.getMessage(), e);
	     }   
	   } else {
	      // unsuccessful bootstrap with SDK 
	        String message;
	        if (event.getCode() == ETException.RAF_INITIALIZE_ENCRYPTION_FAILURE) {
	             message = "ETPush readyAimFire() did not initialize due to an Encryption failure.";
	         } else if (event.getCode() == ETException.RAF_INITIALIZE_ENCRYPTION_OPTOUT_FAILURE) {
	             message = "ETPush readyAimFire() did not initialize encryption failure and unable to opt-out.";
	         } else if (event.getCode() == ETException.RAF_INITIALIZE_EXCEPTION) {
	             message = "ETPush readyAimFire() did not initialize due to an Exception.";
	         } else {
	             message = "ETPush readyAimFire() did not initialize due to an Exception.";
	         }
	         Log.e(TAG, String.format("ETPush readyAimFire() did not initialize due to an Exception with message: %s and code: %d", event.getMessage(), event.getCode()), event.getException());
	         throw new RuntimeException(message);
	   }
	} 

  /**
   * EventBus callback listening for a RegistrationEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final RegistrationEvent event) {
  	Log.i(TAG, "EventBus RegistrationEvent: " + event.toString());

    if (ETPush.getLogLevel() <= Log.DEBUG) {
      Log.d(TAG, "Marketing Cloud update occurred.");
      Log.d(TAG, "Device ID:" + event.getDeviceId());
      Log.d(TAG, "Device Token:" + event.getSystemToken());
      Log.d(TAG, "Subscriber key:" + event.getSubscriberKey());
      for (Object attribute : event.getAttributes()) {
          Log.d(TAG, "Attribute " + ((Attribute) attribute).getKey() + ": [" + ((Attribute) attribute).getValue() + "]");
      }
      Log.d(TAG, "Tags: " + event.getTags());
      Log.d(TAG, "Language: " + event.getLocale());
      Log.d(TAG, String.format("Last sent: %1$d", System.currentTimeMillis()));
    }

  }  

  /**
   * EventBus callback listening for a PushReceivedEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final PushReceivedEvent event) {
  	Log.i(TAG, "EventBus PushReceivedEvent: " + event.toString());

		JSONObject json = new JSONObject();
		Set<String> keys = event.getPayload().keySet();
		for (String key : keys) {
			try {
		  	json.put(key, JSONObject.wrap(event.getPayload().get(key)));
	    } catch (Exception e) {
	  		Log.e(TAG, e.getMessage(), e);
	    } 
		} 

    try {
    	ETPushCordovaPlugin.notificationReceived(json);
    } catch (Exception e) {
  		Log.e(TAG, e.getMessage(), e);
    } 
  }

  /**
   * EventBus callback listening for a GeofenceResponseEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final GeofenceResponseEvent event) {
  	Log.i(TAG, "EventBus GeofenceResponseEvent: " + event.toString());
  } 

  /**
   * EventBus callback listening for a LocationStatusEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final LocationStatusEvent event) {
  	Log.i(TAG, "EventBus LocationStatusEvent: " + event.toString());
  	  	Log.i(TAG, "EventBus LocationStatusEvent isWatching: " + event.isWatchingLocation());
  } 
        
  /**
   * EventBus callback listening for a LocationUpdateEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final LocationUpdateEvent event) {
  	Log.i(TAG, "EventBus LocationUpdateEvent: " + event.toString());
  } 
        
  /**
   * EventBus callback listening for a BeaconRegionEnterEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final BeaconRegionEnterEvent event) {
  	Log.i(TAG, "EventBus BeaconRegionEnterEvent: " + event.toString());
  } 
        
  /**
   * EventBus callback listening for a BeaconRegionExitEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final BeaconRegionExitEvent event) {
  	Log.i(TAG, "EventBus BeaconRegionExitEvent: " + event.toString());
  } 
        
  /**
   * EventBus callback listening for a BeaconRegionRangeEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final BeaconRegionRangeEvent event) {
  	Log.i(TAG, "EventBus BeaconRegionRangeEvent: " + event.toString());
  } 
        
  /**
   * EventBus callback listening for a BeaconResponseEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final BeaconResponseEvent event) {
  	Log.i(TAG, "EventBus BeaconResponseEvent: " + event.toString());
  } 
        
  /**
   * EventBus callback listening for a CloudPagesChangedEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final CloudPagesChangedEvent event) {
  	Log.i(TAG, "EventBus CloudPagesChangedEvent: " + event.toString());     
  } 
        
  /**
   * EventBus callback listening for a LastKnownLocationEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final LastKnownLocationEvent event) {
  	Log.i(TAG, "EventBus LastKnownLocationEvent: " + event.toString());
  } 
        
  /**
   * EventBus callback listening for a ProximityStatusEvent.
   *
   * @param event the type of event we're listening for.
   */
  public void onEvent(final ProximityStatusEvent event) {
  	Log.i(TAG, "EventBus ProximityStatusEvent: " + event.toString());
  } 

  /**
   * Instantiate this class to get a handle on the CloudPageListAdapter
   */
	private class CDVCloudPageListAdapter extends CloudPageListAdapter {
	  public CDVCloudPageListAdapter(Context appContext) {
	    super(appContext);
	  }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {	 
    	return null;
    } 
	}
}

