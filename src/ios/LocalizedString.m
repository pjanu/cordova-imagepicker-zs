//
//  LocalizedString.m
//  ZetBook
//

#import "LocalizedString.h"

@implementation LocalizedString

+ (NSDictionary *)dictionary {
    static NSDictionary *dict = nil;
    NSString *locale = [[[NSLocale preferredLanguages] objectAtIndex:0] componentsSeparatedByString:@"-"].firstObject;

    if(dict == nil)
    {
        NSDictionary *cs = [NSDictionary dictionaryWithObjectsAndKeys:@"Vybráno %d z %d", @"Selected %d of %d",
                            @"Vybráno %d", @"Selected %d",
                            @"Načítám...", @"Loading...",
                            @"Chyba alba: %@ - %@", @"Album Error: %@ - %@",
                            @"Vyberte album", @"Select an Album",
                            @"Vyberte fotku", @"Pick Photo",
                            @"Maximálně %d fotek.", @"Maximum %d photos.",
                            @"Najednou můžete vybrat pouze %d fotek.", @"You can only select %d photos at a time.",
                            @"Okay", @"Okay",
                            @"Hotovo", @"Done",
                            @"Zrušit", @"Cancel",
                            @"Zpět", @"Back",
                            nil];

        NSDictionary *sk = [NSDictionary dictionaryWithObjectsAndKeys:@"Vybráno %d z %d", @"Selected %d of %d",
                            @"Vybráno %d", @"Selected %d",
                            @"Nahrávam...", @"Loading...",
                            @"Chyba alba: %@ - %@", @"Album Error: %@ - %@",
                            @"Vyberte album", @"Select an Album",
                            @"Vyberte fotku", @"Pick Photo",
                            @"Maximálne %d fotografie.", @"Maximum %d photos.",
                            @"Môžete si vybrať iba %d fotografií naraz.", @"You can only select %d photos at a time.",
                            @"Okay", @"Okay",
                            @"Hotovo", @"Done",
                            @"Zrušiť", @"Cancel",
                            @"Späť", @"Back",
                            nil];

        dict = [NSDictionary dictionaryWithObjectsAndKeys:cs, @"cs", sk, @"sk", nil];
    }

    return [dict objectForKey:locale];
}

+ (NSString *)get:(NSString *)key {
    NSDictionary *dictionary = [LocalizedString dictionary];

    if(dictionary == nil)
    {
        return key;
    }

    return [dictionary valueForKey:key];
}

@end
