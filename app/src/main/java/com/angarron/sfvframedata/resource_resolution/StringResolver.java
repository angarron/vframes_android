package com.angarron.sfvframedata.resource_resolution;

import com.angarron.sfvframedata.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andy on 12/23/15
 */
public class StringResolver {

    private static Map<String, Integer> stringIdMap;

    static {
        stringIdMap = new HashMap<>();

        //General Movelist
        stringIdMap.put("id_close_to_opponent", R.string.close_to_opponent);
        stringIdMap.put("id_can_be_done_in_air", R.string.can_be_done_in_air);
        stringIdMap.put("id_during_jump", R.string.during_jump);
        stringIdMap.put("id_during_guard", R.string.during_guard);

        //Karin Movelist
        stringIdMap.put("id_karin_tenko_posttext", R.string.karin_tenko_posttext);
        stringIdMap.put("id_karin_meioken_posttext", R.string.karin_meioken_posttext);

        //Dhalsim Movelist
        stringIdMap.put("id_dhalsim_gale_pretext", R.string.dhalsim_gale_pretext);
    }

    public static int getStringId(String key) {
        if (stringIdMap.containsKey(key)) {
            return stringIdMap.get(key);
        } else {
            throw new RuntimeException("could not find key: " + key);
        }
    }
}