//@flow
import {NativeModules} from 'react-native';

const {RNKakaoSignin} = NativeModules;

export type LoginResult = {
  expire: number,
  accessToken: string,
};

export type OptionalBoolean = 'NONE'
  | 'TRUE'
  | 'FALSE';

export type Profile = {
  id: number,
  nickname: string,
  email: string,
  displayId: string,
  phoneNumber: string,
  emailVerified: OptionalBoolean,
  kakaoTalkUser: OptionalBoolean,
  profileImagePath: string,
  thumbnailImagePath: string,
  signedUp: OptionalBoolean,
}

console.log(RNKakaoSignin);

export default {
  login(): Promise<LoginResult> {
    return RNKakaoSignin.login();
  },

  logout(): Promise<void> {
    return RNKakaoSignin.logout();
  },

  /**
   * OAuth UserID 를 포함한 정보들을 가져 옴
   * @returns {*|Promise<Profile>}
   */
  getProfile(): Promise<Profile> {
    return RNKakaoSignin.getProfile();
  },
};
