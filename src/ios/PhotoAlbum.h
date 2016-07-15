//
//  PhotoAlbum.h
//  ZetBook
//

@protocol PhotoAlbum <NSObject>

- (NSString *)getTitle;
- (NSUInteger)getCount;
- (NSArray *)getPhotos;
- (UIImage *)getThumbnail;

@end