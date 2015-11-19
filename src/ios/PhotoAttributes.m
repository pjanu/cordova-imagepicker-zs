//
//  PhotoAttributes.m
//  ZetBook
//

#import "PhotoAttributes.h"

@implementation PhotoAttributes

-(id)init {
    return [self initWithFilePath:@""];
}

-(id)initWithFilePath:(NSString *)filePath {
    self = [super init];

    if(self)
    {
        self.largePhotoName = filePath;
        self.thumbnailName = filePath;
        self.miniPhotoName = filePath;
        self.originalFilePath = filePath;
        self.originalPhotoWidth = @0;
        self.originalPhotoHeight = @0;
        self.finalWidth = @0;
        self.finalHeight = @0;
    }

    return self;
}

-(NSDictionary *)toDictionary {
    return [NSDictionary dictionaryWithObjectsAndKeys:self.largePhotoName, @"largePhotoName",
            self.thumbnailName, @"thumbnailName",
            self.miniPhotoName, @"miniPhotoName",
            self.originalFilePath, @"originalFilePath",
            self.originalPhotoWidth, @"originalPhotoWidth",
            self.originalPhotoHeight, @"originalPhotoHeight",
            self.finalWidth, @"finalWidth",
            self.finalHeight, @"finalHeight",
            nil];
}

-(NSString *)toJSONString {
    NSDictionary *map = [self toDictionary];
    NSError *error;
    NSData *json = [NSJSONSerialization dataWithJSONObject:map options:NSJSONWritingPrettyPrinted error:&error];
    return [[NSString alloc] initWithData:json encoding:NSUTF8StringEncoding];
}

@end
