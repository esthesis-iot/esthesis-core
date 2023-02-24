import {KeystoreEntryDto} from "./keystore-entry-dto";

export interface KeystoreDto {
  name: string;
  description: string;
  password: string;
  entries: KeystoreEntryDto[];
  version: number;
  type: string;
}
