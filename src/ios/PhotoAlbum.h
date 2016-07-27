//
//  PhotoAlbum.h
//  ZetBook
//

@protocol PhotoAlbum <NSObject>

typedef void (^ThumbnailFetch)(UIImage *);

- (NSString *)getTitle;
- (NSUInteger)getCount;
- (NSMutableArray *)getPhotos;
- (void)fetchThumbnail:(ThumbnailFetch)onThumbnailFetch;

@end
