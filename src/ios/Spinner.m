//
//  Spinner.m
//  ZetBook
//

#import "Spinner.h"

@implementation Spinner

- (id)init:(UIActivityIndicatorViewStyle)style withSize:(float)size withBackgroundColor:(UIColor *)backgroundColor {
    self = [super init];

    if(self)
    {
        self.indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:style];
        self.indicator.hidesWhenStopped = YES;
        self.indicator.frame = CGRectMake(0.0, 0.0, size, size);
        [self.indicator setCenter:self.getScreenCenter];
        [self.indicator setBackgroundColor:backgroundColor];
        [self.getMainWindow addSubview:self.indicator];
    }

    return self;
}

- (void)show {
    [self spinnerAction:@selector(showSpinner)];
}

- (void)hide {
    [self spinnerAction:@selector(hideSpinner)];
}

- (void)spinnerAction:(SEL)method {
    [NSThread detachNewThreadSelector:method toTarget:self withObject:nil];
}

- (void)showSpinner {
    [self.indicator startAnimating];
}

- (void)hideSpinner {
    [self.indicator stopAnimating];
}

- (CGPoint)getScreenCenter {
    UIScreen *screen = [UIScreen mainScreen];
    CGSize size = screen.bounds.size;
    return CGPointMake(size.width * 0.5, size.height * 0.5);
}

- (UIWindow *)getMainWindow {
    return [UIApplication sharedApplication].windows.firstObject;
}

@end
