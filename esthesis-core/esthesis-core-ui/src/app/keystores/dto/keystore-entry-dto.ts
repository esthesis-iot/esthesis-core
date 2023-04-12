export interface KeystoreEntryDto {
  id: string;
  name: string; // Hardware id for devices, name for certificates/CAs.
  password: string; // The password for this entry.
  resourceType: string; // as per AppConstants.KEYSTORE.ITEM.RESOURCE_TYPE
  keyType: string[]; // as per AppConstants.KEYSTORE.ITEM.KEY_TYPE
}
