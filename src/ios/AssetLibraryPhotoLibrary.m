//
//  AssetLibraryPhotoLibrary.m
//  ZetBook
//

#import "AssetLibraryPhotoLibrary.h"
#import "AssetLibraryPhotoAlbum.h"
#import "LocalizedString.h"

@implementation AssetLibraryPhotoLibrary

- (id)init:(ALAssetsLibrary *)library
{
    self = [super init];
    if(self)
    {
        self.library = library;
    }
    return self;
}

- (NSMutableArray *)fetchAlbums:(AlbumFetch)onAlbumFetch
{
    __block NSMutableArray *albums = [[NSMutableArray alloc] init];

    dispatch_async(dispatch_get_main_queue(), ^{
        @autoreleasepool
        {
            void (^enumerator)(ALAssetsGroup *, BOOL *) = ^(ALAssetsGroup *group, BOOL *stop)
            {
                if(group == nil)
                {
                    return;
                }

                // added fix for camera albums order
                NSString *sGroupPropertyName = (NSString *)[group valueForProperty:ALAssetsGroupPropertyName];
                NSUInteger nType = [[group valueForProperty:ALAssetsGroupPropertyType] intValue];
                [group setAssetsFilter:[ALAssetsFilter allPhotos]];

                NSObject<PhotoAlbum> *album = [[AssetLibraryPhotoAlbum alloc] init:group];

                if([[sGroupPropertyName lowercaseString] isEqualToString:@"camera roll"] && nType == ALAssetsGroupSavedPhotos)
                {
                    [albums insertObject:album atIndex:0];
                }
                else
                {
                    [albums addObject:album];
                }

                onAlbumFetch();
            };

            void (^failure)(NSError *) = ^(NSError *error)
            {
                //TODO mixed responsibility -- better to move UI related code to ImageController
                UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Error" message:[NSString stringWithFormat:[LocalizedString get:@"Album Error: %@ - %@"], [error localizedDescription], [error localizedRecoverySuggestion]] delegate:nil cancelButtonTitle:@"Ok" otherButtonTitles:nil];
                [alert show];

                NSLog(@"A problem occured %@", [error description]);
            };

            [self.library enumerateGroupsWithTypes:ALAssetsGroupAll usingBlock:enumerator failureBlock:failure];
        }
    });

    return albums;
}

@end