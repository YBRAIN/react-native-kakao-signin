
package com.ybrain.rn.kakaosignin;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.kakao.auth.ApiResponseCallback;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthService;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RNKakaoSigninModule extends ReactContextBaseJavaModule implements ActivityEventListener {
  private static final String TAG = "RNKakaoSignin";

  private final ReactApplicationContext reactAppContext;

  private SessionCallback sessionCallback;
  private Promise loginPromise;

  public RNKakaoSigninModule(ReactApplicationContext reactAppContext) {
    super(reactAppContext);
    this.reactAppContext = reactAppContext;
    reactAppContext.addActivityEventListener(this);

    KakaoSDK.init(new KakaoSDKAdapter(reactAppContext.getApplicationContext()));

    sessionCallback = new SessionCallback();
    Session.getCurrentSession().addCallback(sessionCallback);
    Session.getCurrentSession().checkAndImplicitOpen();
  }

  @Override
  public void onCatalystInstanceDestroy() {
    reactAppContext.removeActivityEventListener(this);
    Session.getCurrentSession().addCallback(sessionCallback);
  }

  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
      return;
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    // Do nothing
  }

  @Override
  public String getName() {
    return "RNKakaoSignin";
  }

  @ReactMethod
  private void login(final Promise promise) {
    loginPromise = promise;

    final List<AuthType> authTypes = getAuthTypes();
    if (authTypes.size() == 1) {
      Session.getCurrentSession().open(authTypes.get(0), getCurrentActivity());
    } else {
      final Item[] authItems = createAuthItemArray(authTypes);
      ListAdapter adapter = createLoginAdapter(authItems);
      final Dialog dialog = createLoginDialog(authItems, adapter);
      dialog.show();
    }
  }

  /**
   * 실제로 유저에게 보여질 dialog 객체를 생성한다.
   *
   * @param authItems 가능한 AuthType들의 정보를 담고 있는 Item array
   * @param adapter   Dialog의 list view에 쓰일 adapter
   * @return 로그인 방법들을 팝업으로 보여줄 dialog
   */
  private Dialog createLoginDialog(final Item[] authItems, final ListAdapter adapter) {
    final Dialog dialog = new Dialog(getCurrentActivity(), com.kakao.usermgmt.R.style.LoginDialog);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(com.kakao.usermgmt.R.layout.layout_login_dialog);
    if (dialog.getWindow() != null) {
      dialog.getWindow().setGravity(Gravity.CENTER);
    }

    ListView listView = (ListView) dialog.findViewById(com.kakao.usermgmt.R.id.login_list_view);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final AuthType authType = authItems[position].authType;
        if (authType != null) {
          openSession(authType);
        }
        dialog.dismiss();
      }
    });

    Button closeButton = dialog.findViewById(com.kakao.usermgmt.R.id.login_close_button);
    closeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });
    return dialog;
  }

  private ListAdapter createLoginAdapter(final Item[] authItems) {
    return new ArrayAdapter<Item>(
      reactAppContext,
      android.R.layout.select_dialog_item,
      android.R.id.text1, authItems) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
          LayoutInflater inflater = (LayoutInflater) getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          convertView = inflater.inflate(com.kakao.usermgmt.R.layout.layout_login_item, parent, false);
        }
        ImageView imageView = convertView.findViewById(com.kakao.usermgmt.R.id.login_method_icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          imageView.setImageDrawable(reactAppContext.getResources().getDrawable(authItems[position].icon, getContext().getTheme()));
        } else {
          imageView.setImageDrawable(reactAppContext.getResources().getDrawable(authItems[position].icon));
        }
        TextView textView = convertView.findViewById(com.kakao.usermgmt.R.id.login_method_text);
        textView.setText(authItems[position].textId);
        return convertView;
      }
    };
  }

  private Item[] createAuthItemArray(final List<AuthType> authTypes) {
    final List<Item> itemList = new ArrayList<Item>();
    if (authTypes.contains(AuthType.KAKAO_TALK)) {
      itemList.add(new Item(com.kakao.usermgmt.R.string.com_kakao_kakaotalk_account, com.kakao.usermgmt.R.drawable.talk, com.kakao.usermgmt.R.string.com_kakao_kakaotalk_account_tts, AuthType.KAKAO_TALK));
    }
    if (authTypes.contains(AuthType.KAKAO_STORY)) {
      itemList.add(new Item(com.kakao.usermgmt.R.string.com_kakao_kakaostory_account, com.kakao.usermgmt.R.drawable.story, com.kakao.usermgmt.R.string.com_kakao_kakaostory_account_tts, AuthType.KAKAO_STORY));
    }
    if (authTypes.contains(AuthType.KAKAO_ACCOUNT)) {
      itemList.add(new Item(com.kakao.usermgmt.R.string.com_kakao_other_kakaoaccount, com.kakao.usermgmt.R.drawable.account, com.kakao.usermgmt.R.string.com_kakao_other_kakaoaccount_tts, AuthType.KAKAO_ACCOUNT));
    }

    return itemList.toArray(new Item[itemList.size()]);
  }

  private void openSession(final AuthType authType) {
    Session.getCurrentSession().open(authType, getCurrentActivity());
  }

  // Login session result listener
  private class SessionCallback implements ISessionCallback {
    @Override
    public void onSessionOpened() {
      Log.d(TAG, "Success to open login session");
      if (loginPromise != null) {
        AccessToken tokenInfo = Session.getCurrentSession().getTokenInfo();
        WritableMap result = new WritableNativeMap();
        result.putString("accessToken", tokenInfo.getAccessToken());
        result.putDouble("expire", tokenInfo.accessTokenExpiresAt().getTime());
        loginPromise.resolve(result);
        loginPromise = null;
      }
    }

    @Override
    public void onSessionOpenFailed(KakaoException e) {
      Log.e(TAG, "Failed to open login session");
      Logger.e(e);
      if (loginPromise != null) {
        loginPromise.reject(e);
        loginPromise = null;
      }
    }
  }

  private List<AuthType> getAuthTypes() {
    final List<AuthType> availableAuthTypes = new ArrayList<>();
    if (Session.getCurrentSession().getAuthCodeManager().isTalkLoginAvailable()) {
      availableAuthTypes.add(AuthType.KAKAO_TALK);
    }
    if (Session.getCurrentSession().getAuthCodeManager().isStoryLoginAvailable()) {
      availableAuthTypes.add(AuthType.KAKAO_STORY);
    }
    availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);

    AuthType[] authTypes = KakaoSDK.getAdapter().getSessionConfig().getAuthTypes();
    if (authTypes == null || authTypes.length == 0 || (authTypes.length == 1 && authTypes[0] == AuthType.KAKAO_LOGIN_ALL)) {
      authTypes = AuthType.values();
    }
    availableAuthTypes.retainAll(Arrays.asList(authTypes));

    // 개발자가 설정한 것과 available 한 타입이 없다면 직접계정 입력이 뜨도록 한다.
    if (availableAuthTypes.size() == 0) {
      availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);
    }

    return availableAuthTypes;
  }

  @ReactMethod
  private void logout(final Promise promise) {
    UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
      @Override
      public void onCompleteLogout() {
        promise.resolve(null);
      }
    });
  }

  @ReactMethod
  private void getProfile(final Promise promise) {
    UserManagement.getInstance().me(null, new MeV2ResponseCallback() {
      @Override
      public void onSuccess(MeV2Response result) {
        WritableMap map = new WritableNativeMap();
        map.putDouble("id", result.getId());
        map.putString("nickname", result.getNickname());
        map.putString("email", result.getKakaoAccount().getEmail());
        map.putString("displayId", result.getKakaoAccount().getDisplayId());
        map.putString("phoneNumber", result.getKakaoAccount().getPhoneNumber());
        map.putString("emailVerified", result.getKakaoAccount().isEmailVerified().name());
        map.putString("kakaoTalkUser", result.getKakaoAccount().isKakaoTalkUser().name());
        map.putString("profileImagePath", result.getProfileImagePath());
        map.putString("thumbnailImagePath", result.getThumbnailImagePath());
        map.putString("signedUp", result.hasSignedUp().name());
        promise.resolve(map);
      }

      @Override
      public void onSessionClosed(ErrorResult errorResult) {
        promise.reject(new Exception("Code=" + errorResult.getErrorCode()
          + ", Msg=" + errorResult.getErrorMessage()
          + ", HttpResp=" + errorResult.getHttpStatus()
        ));
      }
    });
  }

  private class KakaoSDKAdapter extends KakaoAdapter {
    private final Context context;

    KakaoSDKAdapter(Context appContext) {
      context = appContext;
    }

    /**
     * Session Config에 대해서는 default값들이 존재한다.
     * 필요한 상황에서만 override해서 사용하면 됨.
     *
     * @return Session의 설정값.
     */
    @Override
    public ISessionConfig getSessionConfig() {
      return new ISessionConfig() {
        @Override
        public AuthType[] getAuthTypes() {
          return new AuthType[]{AuthType.KAKAO_LOGIN_ALL};
        }

        @Override
        public boolean isUsingWebviewTimer() {
          return false;
        }

        @Override
        public boolean isSecureMode() {
          return false;
        }

        @Override
        public ApprovalType getApprovalType() {
          return ApprovalType.INDIVIDUAL;
        }

        @Override
        public boolean isSaveFormData() {
          return true;
        }
      };
    }

    @Override
    public IApplicationConfig getApplicationConfig() {
      return new IApplicationConfig() {
        @Override
        public Context getApplicationContext() {
          return context;
        }
      };
    }
  }

  private static class Item {
    final int textId;
    public final int icon;
    final int contentDescId;
    final AuthType authType;

    Item(final int textId, final Integer icon, final int contentDescId, final AuthType authType) {
      this.textId = textId;
      this.icon = icon;
      this.contentDescId = contentDescId;
      this.authType = authType;
    }
  }
}


