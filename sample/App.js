/**
 * Sample React Native Kakao sign in
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {Alert, StyleSheet, Text, TouchableOpacity, View} from 'react-native';
import KakaoSignin from '@ybrain/react-native-kakao-signin';

type Props = {};
export default class App extends Component<Props> {
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>@ybrain/Kakao Signin Sample</Text>

        <Button
          title={'로그인'}
          onPress={async () => {
            const result = await KakaoSignin.login();
            Alert.alert('로그인 결과', JSON.stringify(result));
          }}
        />

        <Button
          title={'프로필 정보 가져오기'}
          onPress={async () => {
            const result = await KakaoSignin.getProfile();
            Alert.alert('프로필 정보 결과', JSON.stringify(result));
          }}
        />

        <Button
          title={'로그아웃'}
          onPress={async () => {
            await KakaoSignin.logout();
          }}
        />
      </View>
    );
  }
}

const Button = (props) =>
  <TouchableOpacity
    style={{
      margin: 10,
      padding: 10,
      backgroundColor: 'yellow',
    }}
    onPress={props.onPress}>
    <Text
      style={{fontSize: 16, textAlign: 'center'}}
    >{props.title}</Text>
  </TouchableOpacity>;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 40,
    justifyContent: 'center',
    alignItems: 'stretch',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});
