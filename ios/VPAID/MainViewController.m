//
//  Copyright (c) 2015 SpotX, Inc. All rights reserved.
//

#import "MainViewController.h"

@implementation MainViewController

- (void)playAd:(id)sender
{
  NSString *channelID = self.channel.text;
  if (channelID.length) {

    VPAIDViewController *vc = [[VPAIDViewController alloc] initWithNibName:nil bundle:nil];
    vc.delegate = self;
    vc.channelID = channelID;
    vc.domain = @"com.spotxchange.vpaid";
    vc.secure = self.secure.isOn;
    [vc startLoading];

    [self presentViewController:vc animated:YES completion:nil];
  }
}

- (void)vpaidViewController:(VPAIDViewController *)viewController didReceiveEvent:(NSString *)event
{
  NSArray *eof = @[@"AdStopped", @"AdError", @"AdCompleted"];
  if ([eof containsObject:event]) {
    [self dismissViewControllerAnimated:NO completion:nil];
  }
}

@end
