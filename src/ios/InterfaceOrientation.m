//
//  InterfaceOrientation.m
//  ZetBook
//

#import "InterfaceOrientation.h"

@implementation InterfaceOrientation

const NSString *LANDSCAPE = @"landscape";
const NSString *PORTRAIT = @"portrait";

-(id)init {
    self = [super init];
    if(self)
    {
        self.mask = UIInterfaceOrientationMaskAll;
    }
    return self;
}

-(id)initWithOrientation:(NSString *)orientation {
    self = [super init];
    if(self)
    {
        [self setOrientation:orientation];
    }
    return self;
}

+(id)interfaceOrientationWithOrientation:(NSString *)orientation {
    return [[InterfaceOrientation alloc] initWithOrientation:orientation];
}

-(UIInterfaceOrientationMask)getMask {
    return self.mask;
}

-(void)setOrientation:(NSString *)orientation {
    if([LANDSCAPE isEqualToString:orientation])
    {
        self.mask = UIInterfaceOrientationMaskLandscape;
    }
    else if([PORTRAIT isEqualToString:orientation])
    {
        self.mask = UIInterfaceOrientationMaskPortrait | UIInterfaceOrientationMaskPortraitUpsideDown;
    }
    else
    {
        self.mask = UIInterfaceOrientationMaskAll;
    }
}

@end