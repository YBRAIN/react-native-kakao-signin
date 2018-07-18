
#import "RNKakaoSignin.h"
#import <React/RCTLog.h>
#import <KakaoOpenSDK/KakaoOpenSDK.h>

@implementation RNKakaoSignin

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_REMAP_METHOD(login,
                 loginWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    KOSession *session = [KOSession sharedSession];
    [session openWithCompletionHandler:^(NSError *error) {
        if ([[KOSession sharedSession] isOpen]) {
            NSDate* expire = session.token.accessTokenExpiresAt;
            
            // signIn success
            NSString* result = [NSString stringWithFormat:@"{accessToken: %@}", session.token.accessToken];
            
            NSDictionary *dict = @{
                                   @"accessToken" : session.token.accessToken,
                                   };

            
            resolve(dict);
        } else {
            RCTLogInfo(@"error=%@", error);
            reject(@"Login failed", @"Sign in failed", error);
        }
    }];
}

RCT_REMAP_METHOD(logout,
                 logoutWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    [[KOSession sharedSession] close];
    resolve(nil);
}


RCT_REMAP_METHOD(getProfile,
                 getProfileResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    [KOSessionTask userMeTaskWithCompletion:^(NSError *error, KOUserMe *me) {
        if (error) {
            reject(@"Failed to get profile",@"Failed to get profile", error);
        } else {
            NSDictionary *dict = @{
                                   @"id" : me.ID,
                                   @"nickname": me.nickname,
                                   @"profileImagePath": me.profileImageURL,
                                   @"thumbImagePath": me.thumbnailImageURL,
                                   };
            resolve(dict);
        }
    }];
}

@end
  
