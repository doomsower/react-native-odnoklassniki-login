#import "RNOdnoklassniki.h"
#import "OKSDK.h"
#import <RCTUtils.h>

#ifdef DEBUG
#define DMLog(...) NSLog(@"[VKLogin] %s %@", __PRETTY_FUNCTION__, [NSString stringWithFormat:__VA_ARGS__])
#else
#define DMLog(...) do { } while (0)
#endif

@implementation RNOdnoklassniki {
  RCTPromiseResolveBlock loginResolver;
  RCTPromiseRejectBlock loginRejector;
}

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(initialize: (NSString *) appId withKey: (NSString *) appKey ) {
  DMLog(@"Initialize app id %@", appId);
  UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
  OKSDKInitSettings *settings = [OKSDKInitSettings new];
  settings.appId = appId;
  settings.appKey = appKey;
  settings.controllerHandler = ^{
    return root;
  };
  [OKSDK initWithSettings: settings];
}

RCT_EXPORT_METHOD(login: (NSArray *) scope resolver: (RCTPromiseResolveBlock) resolve rejecter: (RCTPromiseRejectBlock) reject) {
  DMLog(@"Login with scope %@", scope);
  self->loginResolver = resolve;
  self->loginRejector = reject;

  [OKSDK authorizeWithPermissions:scope success:^(id data) {
    [OKSDK invokeMethod:@"users.getCurrentUser" arguments:@{} success:^(NSDictionary* data) {
      DMLog(@"Successfully obtained current user");
      self->loginResolver([self getResponse:data]);
    } error:^(NSError *error) {
      DMLog(@"Error in users.getCurrentUser: %@", [error localizedDescription]);
      self->loginRejector(RCTErrorUnspecified, nil, RCTErrorWithMessage([error localizedDescription]));
    }];
  } error:^(NSError *error) {
    DMLog(@"Error during auth: %@", [error localizedDescription]);
    self->loginRejector(RCTErrorUnspecified, nil, RCTErrorWithMessage([error localizedDescription]));
  }];
};

RCT_REMAP_METHOD(logout, resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
  DMLog(@"Logout");
  [OKSDK clearAuth];
  resolve(nil);
};

- (NSDictionary *)getResponse:(NSDictionary *)user {
  return @{
    @"access_token" : [OKSDK currentAccessToken],
    @"session_secret_key" : [OKSDK currentAccessTokenSecretKey],
    @"user": user
  };
}

@end
