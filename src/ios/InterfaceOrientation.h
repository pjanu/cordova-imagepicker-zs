//
//  InterfaceOrientation.h
//  ZetBook
//

@interface InterfaceOrientation : NSObject

@property UIInterfaceOrientationMask mask;

-(id)init;
-(id)initWithOrientation:(NSString *)orientation;
+(id)interfaceOrientationWithOrientation:(NSString *)orientation;

-(UIInterfaceOrientationMask)getMask;
-(void)setOrientation:(NSString *)orientation;

@end