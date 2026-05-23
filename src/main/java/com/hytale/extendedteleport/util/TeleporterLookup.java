package com.hytale.extendedteleport.util;

import com.hytale.extendedteleport.TeleporterManager;
import com.hytale.extendedteleport.data.TeleporterInfo;
import javax.annotation.Nullable;

public final class TeleporterLookup {
   private TeleporterLookup() {
   }

   @Nullable
   public static TeleporterInfo findExact(TeleporterManager manager, String name) {
      return find(manager, name, TeleporterLookup.SearchMode.EXACT);
   }

   @Nullable
   public static TeleporterInfo findFlexible(TeleporterManager manager, String name) {
      return find(manager, name, TeleporterLookup.SearchMode.FLEXIBLE);
   }

   @Nullable
   public static TeleporterInfo find(TeleporterManager manager, String name, TeleporterLookup.SearchMode mode) {
      if (name != null && !name.isEmpty()) {
         String lowerName = name.toLowerCase();

         for (TeleporterInfo info : manager.getAllTeleporters()) {
            String warpName = info.warpName();
            if (warpName != null && warpName.toLowerCase().equals(lowerName)) {
               return info;
            }
         }

         for (TeleporterInfo info : manager.getAllTeleporters()) {
            if (info.displayName().toLowerCase().equals(lowerName)) {
               return info;
            }
         }

         if (mode == TeleporterLookup.SearchMode.EXACT) {
            return null;
         }

         for (TeleporterInfo info : manager.getAllTeleporters()) {
            String warpName = info.warpName();
            if (warpName != null && warpName.toLowerCase().contains(lowerName)) {
               return info;
            }
         }

         for (TeleporterInfo info : manager.getAllTeleporters()) {
            if (info.locationKey().toLowerCase().contains(lowerName)) {
               return info;
            }
         }

         for (TeleporterInfo info : manager.getAllTeleporters()) {
            if (info.displayName().toLowerCase().contains(lowerName)) {
               return info;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public enum SearchMode {
      EXACT,
      FLEXIBLE;
   }
}
