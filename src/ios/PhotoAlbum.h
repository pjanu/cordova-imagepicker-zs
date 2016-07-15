//
//  PhotoAlbum.h
//  ZetBook
//

@protocol PhotoAlbum <NSObject>

- (NSString *)getTitle;
- (NSUInteger)getCount;
- (NSMutableArray *)getPhotos;
- (UIImage *)getThumbnail;

@end
