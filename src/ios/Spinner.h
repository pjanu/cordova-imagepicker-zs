//
//  Spinner.h
//  ZetBook
//

@interface Spinner : NSObject

@property (nonatomic, strong) UIActivityIndicatorView *indicator;

- (id)init:(UIActivityIndicatorViewStyle)style withSize:(float)size withBackgroundColor:(UIColor *)backgroundColor;
- (void)show;
- (void)hide;

@end
