//
//  PhotoLibrary.h
//  ZetBook
//

@protocol PhotoLibrary <NSObject>

typedef void (^ AlbumFetch)();

- (NSMutableArray *)fetchAlbums:(AlbumFetch)onAlbumFetch;

@end