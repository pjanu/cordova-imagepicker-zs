//
//  PhotoResize.m
//  ZetBook
//

#import "PhotoResize.h"

@implementation PhotoResize

- (id) initWithCGSize:(CGSize)size {
    self = [super init];

    if(self)
    {
        self.size = size;
    }

    return self;
}

+ (id) resizeWithCGSize:(CGSize)size {
    return [[PhotoResize alloc] initWithCGSize:size];
}

+ (id) resizeForThumbnail {
    return [[PhotoResize alloc] initWithCGSize:CGSizeMake(300, 300)];
};

+ (id) resizeForMini {
    return [[PhotoResize alloc] initWithCGSize:CGSizeMake(30, 30)];
};

@end
