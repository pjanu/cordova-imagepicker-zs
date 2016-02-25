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
                            @"%d", @"%d",
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
                            @"%d", @"%d",
                            nil];

        NSDictionary *de = [NSDictionary dictionaryWithObjectsAndKeys:@"%d von %d ausgewählt", @"Selected %d of %d",
                            @"%d ausgewählt", @"Selected %d",
                            @"Wird geladen...", @"Loading...",
                            @"Album-Fehler: %@ - %@", @"Album Error: %@ - %@",
                            @"Wählen Sie ein Album", @"Select an Album",
                            @"Wählen Sie ein Foto", @"Pick Photo",
                            @"Maximal %d Fotos.", @"Maximum %d photos.",
                            @"Sie können nur %d Fotos auf einmal auswählen.", @"You can only select %d photos at a time.",
                            @"Okay", @"Okay",
                            @"Fertig", @"Done",
                            @"Abbrechen", @"Cancel",
                            @"Zurück", @"Back",
                            @"%d", @"%d",
                            nil];

        NSDictionary *hu = [NSDictionary dictionaryWithObjectsAndKeys:@"Kiválasztva %d a %d-ból", @"Selected %d of %d",
                            @"Kiválasztva %d", @"Selected %d",
                            @"Beolvasás", @"Loading...",
                            @"Album hiba: %@ - %@", @"Album Error: %@ - %@",
                            @"Válasszon albumot", @"Select an Album",
                            @"Válasszon fotót", @"Pick Photo",
                            @"Maximum %d fotó.", @"Maximum %d photos.",
                            @"Egyszerre csak %d fotót választhat.", @"You can only select %d photos at a time.",
                            @"Rendben", @"Okay",
                            @"Kész", @"Done",
                            @"Törlés", @"Cancel",
                            @"Vissza", @"Back",
                            @"%d", @"%d",
                            nil];

        NSDictionary *pl = [NSDictionary dictionaryWithObjectsAndKeys:@"Wybrane %d z %d", @"Selected %d of %d",
                            @"Wybrane %d", @"Selected %d",
                            @"Wczytuję...", @"Loading...",
                            @"Błąd albumu: %@ - %@", @"Album Error: %@ - %@",
                            @"Wybierz album", @"Select an Album",
                            @"Wybierz zdjęcie", @"Pick Photo",
                            @"Maksymalnie %d zdjęć.", @"Maximum %d photos.",
                            @"Możesz wybrać naraz tylko %d zdjęć.", @"You can only select %d photos at a time.",
                            @"Okay", @"Okay",
                            @"Gotowe", @"Done",
                            @"Anuluj", @"Cancel",
                            @"Wstecz", @"Back",
                            @"%d", @"%d",
                            nil];

        dict = [NSDictionary dictionaryWithObjectsAndKeys:cs, @"cs", sk, @"sk", de, @"de", pl, @"pl", hu, @"hu", nil];
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
