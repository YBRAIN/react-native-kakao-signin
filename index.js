//@flow
import {NativeModules} from 'react-native';

const {RNKakaoSignin} = NativeModules;

export type LoginResult = {
  expire: number,
  accessToken: string,
};

export type ProfileKey =
  /**카카오톡 또는 카카오스토리의 닉네임 정보*/
  'properties.nickname'
  /**640px * 640px 크기의 프로필 이미지 URL (2017/08/22 이전 가입자에 대해서는 480px * 480px ~ 1024px * 1024px 크기를 가질 수 있음)*/
  | 'properties.profile_image'
  /**110px * 110px 크기의 썸네일 프로필 이미지 URL (2017/08/22 이전 가입자에 대해서는 160px * 213px 크기를 가질 수 있음)*/
  | 'properties.thumbnail_image'
  /**사용자 카카오계정의 이메일 소유여부, 이메일 값, 이메일 인증여부, 이메일 유효여부*/
  | 'kakao_account.email'
  /**range  사용자 카카오계정의 연령대 소유여부, 연령대 값*/
  | 'kakao_account.age_range'
  /**사용자 카카오계정의 생일 소유여부, 생일 값*/
  | 'kakao_account.birthday'
  /**사용자 카카오계정의 성별 소유여부, 성별 값*/
  | 'kakao_account.gender';

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
   * @param keys 가져오고 싶은 일부 필드만 지정할 수 있음. 지정하지 않은 필드는 null이 됨
   * @returns {*|Promise<Profile>}
   */
  getProfile(keys?: ProfileKey[]): Promise<Profile> {
    return RNKakaoSignin.getProfile();
  },
};

