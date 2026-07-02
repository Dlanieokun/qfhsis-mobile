package com.android.hfsis.model.idpcs.rabies;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single "Individual Treatment Record" from the Capoocan
 * Municipal Health Office Animal Bite Treatment Center form. Field names
 * and meaning mirror the sections of the source paper form:
 *
 * -- Patient Information --
 * registryNo, name, age, sex, civilStatus, address, birthdate, birthPlace,
 * contactNo, philhealthNo, weightKg, bloodPressure
 *
 * -- History of Exposure --
 * dateOfBite (mm/dd/yy), timeOfBite, placeOfBite
 *
 * -- Nature of Injury (checkboxes) --
 * injuryScratch, injuryAbrasion, injuryLaceration, injuryPunctured,
 * injuryAvulsed, injuryOthers, injuryOthersSpecify
 *
 * -- Wound & Animal Information --
 * woundStatus ("Bleeding"/"Non-Bleeding")
 * woundWashing ("Done"/"Not done")
 * bitingAnimal ("Dog"/"Cat"/"Others") + bitingAnimalOthersSpecify
 * ownershipStatus ("Owned/Pet"/"Stray")
 * animalStatusAtBite ("Alive"/"Weak"/"Lost")
 * animalStatusAtConsult ("Alive"/"Killed"/"Died") + animalDiedDate
 * animalVaccination ("Yes"/"No"/"Unknown") + animalVaccinationDate
 *
 * -- Patient's Condition to Consider (checkboxes) --
 * conditionEpilepsy, conditionDm, conditionHypertension, conditionAsthma,
 * conditionAlcoholic, conditionEggAllergy
 *
 * -- Medical Treatment: PVRV (Purified Verocell Rabies Vaccine) --
 * pvrvDay0Date/Batch, pvrvDay3Date/Batch, pvrvDay7Date/Batch,
 * pvrvDay28Date/Batch, pvrvOutcome
 *
 * -- Medical Treatment: PCEV (Purified Chick Embryo Vaccine) --
 * pcevDay0Date/Batch, pcevDay3Date/Batch, pcevDay7Date/Batch,
 * pcevDay28Date/Batch, pcevOutcome
 *
 * -- Additional Treatment & Impression --
 * erig, hrig, tetanusToxoidDate, atsDose, atsDate, impression
 */
@Entity(tableName = "rabies_records")
public class RabiesRecord {@PrimaryKey(autoGenerate = true)
@SerializedName("id")
private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("age")
    private int age;

    @SerializedName("sex")
    private String sex;

    @SerializedName("civil_status")
    private String civilStatus;

    @SerializedName("address")
    private String address;

    @SerializedName("birthdate")
    private String birthdate;

    @SerializedName("birth_place")
    private String birthPlace;

    @SerializedName("contact_no")
    private String contactNo;

    @SerializedName("philhealth_no")
    private String philhealthNo;

    @SerializedName("weight_kg")
    private String weightKg;

    @SerializedName("blood_pressure")
    private String bloodPressure;

    @SerializedName("date_of_bite")
    private String dateOfBite;

    @SerializedName("time_of_bite")
    private String timeOfBite;

    @SerializedName("place_of_bite")
    private String placeOfBite;

    @SerializedName("injury_scratch")
    private boolean injuryScratch;

    @SerializedName("injury_abrasion")
    private boolean injuryAbrasion;

    @SerializedName("injury_laceration")
    private boolean injuryLaceration;

    @SerializedName("injury_punctured")
    private boolean injuryPunctured;

    @SerializedName("injury_avulsed")
    private boolean injuryAvulsed;

    @SerializedName("injury_others")
    private boolean injuryOthers;

    @SerializedName("injury_others_specify")
    private String injuryOthersSpecify;

    @SerializedName("wound_status")
    private String woundStatus;

    @SerializedName("wound_washing")
    private String woundWashing;

    @SerializedName("biting_animal")
    private String bitingAnimal;

    @SerializedName("biting_animal_others_specify")
    private String bitingAnimalOthersSpecify;

    @SerializedName("ownership_status")
    private String ownershipStatus;

    @SerializedName("animal_status_at_bite")
    private String animalStatusAtBite;

    @SerializedName("animal_status_at_consult")
    private String animalStatusAtConsult;

    @SerializedName("animal_died_date")
    private String animalDiedDate;

    @SerializedName("animal_vaccination")
    private String animalVaccination;

    @SerializedName("animal_vaccination_date")
    private String animalVaccinationDate;

    @SerializedName("condition_epilepsy")
    private boolean conditionEpilepsy;

    @SerializedName("condition_dm")
    private boolean conditionDm;

    @SerializedName("condition_hypertension")
    private boolean conditionHypertension;

    @SerializedName("condition_asthma")
    private boolean conditionAsthma;

    @SerializedName("condition_alcoholic")
    private boolean conditionAlcoholic;

    @SerializedName("condition_egg_allergy")
    private boolean conditionEggAllergy;

    @SerializedName("pvrv_day0_date")
    private String pvrvDay0Date;

    @SerializedName("pvrv_day0_batch")
    private String pvrvDay0Batch;

    @SerializedName("pvrv_day3_date")
    private String pvrvDay3Date;

    @SerializedName("pvrv_day3_batch")
    private String pvrvDay3Batch;

    @SerializedName("pvrv_day7_date")
    private String pvrvDay7Date;

    @SerializedName("pvrv_day7_batch")
    private String pvrvDay7Batch;

    @SerializedName("pvrv_day28_date")
    private String pvrvDay28Date;

    @SerializedName("pvrv_day28_batch")
    private String pvrvDay28Batch;

    @SerializedName("pvrv_outcome")
    private String pvrvOutcome;

    @SerializedName("pcev_day0_date")
    private String pcevDay0Date;

    @SerializedName("pcev_day0_batch")
    private String pcevDay0Batch;

    @SerializedName("pcev_day3_date")
    private String pcevDay3Date;

    @SerializedName("pcev_day3_batch")
    private String pcevDay3Batch;

    @SerializedName("pcev_day7_date")
    private String pcevDay7Date;

    @SerializedName("pcev_day7_batch")
    private String pcevDay7Batch;

    @SerializedName("pcev_day28_date")
    private String pcevDay28Date;

    @SerializedName("pcev_day28_batch")
    private String pcevDay28Batch;

    @SerializedName("pcev_outcome")
    private String pcevOutcome;

    @SerializedName("erig")
    private String erig;

    @SerializedName("hrig")
    private String hrig;

    @SerializedName("tetanus_toxoid_date")
    private String tetanusToxoidDate;

    @SerializedName("ats_dose")
    private String atsDose;

    @SerializedName("ats_date")
    private String atsDate;

    @SerializedName("impression")
    private String impression;

    public RabiesRecord() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCivilStatus() {
        return civilStatus;
    }

    public void setCivilStatus(String civilStatus) {
        this.civilStatus = civilStatus;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getPhilhealthNo() {
        return philhealthNo;
    }

    public void setPhilhealthNo(String philhealthNo) {
        this.philhealthNo = philhealthNo;
    }

    public String getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(String weightKg) {
        this.weightKg = weightKg;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public String getDateOfBite() {
        return dateOfBite;
    }

    public void setDateOfBite(String dateOfBite) {
        this.dateOfBite = dateOfBite;
    }

    public String getTimeOfBite() {
        return timeOfBite;
    }

    public void setTimeOfBite(String timeOfBite) {
        this.timeOfBite = timeOfBite;
    }

    public String getPlaceOfBite() {
        return placeOfBite;
    }

    public void setPlaceOfBite(String placeOfBite) {
        this.placeOfBite = placeOfBite;
    }

    public boolean isInjuryScratch() {
        return injuryScratch;
    }

    public void setInjuryScratch(boolean injuryScratch) {
        this.injuryScratch = injuryScratch;
    }

    public boolean isInjuryAbrasion() {
        return injuryAbrasion;
    }

    public void setInjuryAbrasion(boolean injuryAbrasion) {
        this.injuryAbrasion = injuryAbrasion;
    }

    public boolean isInjuryLaceration() {
        return injuryLaceration;
    }

    public void setInjuryLaceration(boolean injuryLaceration) {
        this.injuryLaceration = injuryLaceration;
    }

    public boolean isInjuryPunctured() {
        return injuryPunctured;
    }

    public void setInjuryPunctured(boolean injuryPunctured) {
        this.injuryPunctured = injuryPunctured;
    }

    public boolean isInjuryAvulsed() {
        return injuryAvulsed;
    }

    public void setInjuryAvulsed(boolean injuryAvulsed) {
        this.injuryAvulsed = injuryAvulsed;
    }

    public boolean isInjuryOthers() {
        return injuryOthers;
    }

    public void setInjuryOthers(boolean injuryOthers) {
        this.injuryOthers = injuryOthers;
    }

    public String getInjuryOthersSpecify() {
        return injuryOthersSpecify;
    }

    public void setInjuryOthersSpecify(String injuryOthersSpecify) {
        this.injuryOthersSpecify = injuryOthersSpecify;
    }

    public String getWoundStatus() {
        return woundStatus;
    }

    public void setWoundStatus(String woundStatus) {
        this.woundStatus = woundStatus;
    }

    public String getWoundWashing() {
        return woundWashing;
    }

    public void setWoundWashing(String woundWashing) {
        this.woundWashing = woundWashing;
    }

    public String getBitingAnimal() {
        return bitingAnimal;
    }

    public void setBitingAnimal(String bitingAnimal) {
        this.bitingAnimal = bitingAnimal;
    }

    public String getBitingAnimalOthersSpecify() {
        return bitingAnimalOthersSpecify;
    }

    public void setBitingAnimalOthersSpecify(String bitingAnimalOthersSpecify) {
        this.bitingAnimalOthersSpecify = bitingAnimalOthersSpecify;
    }

    public String getOwnershipStatus() {
        return ownershipStatus;
    }

    public void setOwnershipStatus(String ownershipStatus) {
        this.ownershipStatus = ownershipStatus;
    }

    public String getAnimalStatusAtBite() {
        return animalStatusAtBite;
    }

    public void setAnimalStatusAtBite(String animalStatusAtBite) {
        this.animalStatusAtBite = animalStatusAtBite;
    }

    public String getAnimalStatusAtConsult() {
        return animalStatusAtConsult;
    }

    public void setAnimalStatusAtConsult(String animalStatusAtConsult) {
        this.animalStatusAtConsult = animalStatusAtConsult;
    }

    public String getAnimalDiedDate() {
        return animalDiedDate;
    }

    public void setAnimalDiedDate(String animalDiedDate) {
        this.animalDiedDate = animalDiedDate;
    }

    public String getAnimalVaccination() {
        return animalVaccination;
    }

    public void setAnimalVaccination(String animalVaccination) {
        this.animalVaccination = animalVaccination;
    }

    public String getAnimalVaccinationDate() {
        return animalVaccinationDate;
    }

    public void setAnimalVaccinationDate(String animalVaccinationDate) {
        this.animalVaccinationDate = animalVaccinationDate;
    }

    public boolean isConditionEpilepsy() {
        return conditionEpilepsy;
    }

    public void setConditionEpilepsy(boolean conditionEpilepsy) {
        this.conditionEpilepsy = conditionEpilepsy;
    }

    public boolean isConditionDm() {
        return conditionDm;
    }

    public void setConditionDm(boolean conditionDm) {
        this.conditionDm = conditionDm;
    }

    public boolean isConditionHypertension() {
        return conditionHypertension;
    }

    public void setConditionHypertension(boolean conditionHypertension) {
        this.conditionHypertension = conditionHypertension;
    }

    public boolean isConditionAsthma() {
        return conditionAsthma;
    }

    public void setConditionAsthma(boolean conditionAsthma) {
        this.conditionAsthma = conditionAsthma;
    }

    public boolean isConditionAlcoholic() {
        return conditionAlcoholic;
    }

    public void setConditionAlcoholic(boolean conditionAlcoholic) {
        this.conditionAlcoholic = conditionAlcoholic;
    }

    public boolean isConditionEggAllergy() {
        return conditionEggAllergy;
    }

    public void setConditionEggAllergy(boolean conditionEggAllergy) {
        this.conditionEggAllergy = conditionEggAllergy;
    }

    public String getPvrvDay0Date() {
        return pvrvDay0Date;
    }

    public void setPvrvDay0Date(String pvrvDay0Date) {
        this.pvrvDay0Date = pvrvDay0Date;
    }

    public String getPvrvDay0Batch() {
        return pvrvDay0Batch;
    }

    public void setPvrvDay0Batch(String pvrvDay0Batch) {
        this.pvrvDay0Batch = pvrvDay0Batch;
    }

    public String getPvrvDay3Date() {
        return pvrvDay3Date;
    }

    public void setPvrvDay3Date(String pvrvDay3Date) {
        this.pvrvDay3Date = pvrvDay3Date;
    }

    public String getPvrvDay3Batch() {
        return pvrvDay3Batch;
    }

    public void setPvrvDay3Batch(String pvrvDay3Batch) {
        this.pvrvDay3Batch = pvrvDay3Batch;
    }

    public String getPvrvDay7Date() {
        return pvrvDay7Date;
    }

    public void setPvrvDay7Date(String pvrvDay7Date) {
        this.pvrvDay7Date = pvrvDay7Date;
    }

    public String getPvrvDay7Batch() {
        return pvrvDay7Batch;
    }

    public void setPvrvDay7Batch(String pvrvDay7Batch) {
        this.pvrvDay7Batch = pvrvDay7Batch;
    }

    public String getPvrvDay28Date() {
        return pvrvDay28Date;
    }

    public void setPvrvDay28Date(String pvrvDay28Date) {
        this.pvrvDay28Date = pvrvDay28Date;
    }

    public String getPvrvDay28Batch() {
        return pvrvDay28Batch;
    }

    public void setPvrvDay28Batch(String pvrvDay28Batch) {
        this.pvrvDay28Batch = pvrvDay28Batch;
    }

    public String getPvrvOutcome() {
        return pvrvOutcome;
    }

    public void setPvrvOutcome(String pvrvOutcome) {
        this.pvrvOutcome = pvrvOutcome;
    }

    public String getPcevDay0Date() {
        return pcevDay0Date;
    }

    public void setPcevDay0Date(String pcevDay0Date) {
        this.pcevDay0Date = pcevDay0Date;
    }

    public String getPcevDay0Batch() {
        return pcevDay0Batch;
    }

    public void setPcevDay0Batch(String pcevDay0Batch) {
        this.pcevDay0Batch = pcevDay0Batch;
    }

    public String getPcevDay3Date() {
        return pcevDay3Date;
    }

    public void setPcevDay3Date(String pcevDay3Date) {
        this.pcevDay3Date = pcevDay3Date;
    }

    public String getPcevDay3Batch() {
        return pcevDay3Batch;
    }

    public void setPcevDay3Batch(String pcevDay3Batch) {
        this.pcevDay3Batch = pcevDay3Batch;
    }

    public String getPcevDay7Date() {
        return pcevDay7Date;
    }

    public void setPcevDay7Date(String pcevDay7Date) {
        this.pcevDay7Date = pcevDay7Date;
    }

    public String getPcevDay7Batch() {
        return pcevDay7Batch;
    }

    public void setPcevDay7Batch(String pcevDay7Batch) {
        this.pcevDay7Batch = pcevDay7Batch;
    }

    public String getPcevDay28Date() {
        return pcevDay28Date;
    }

    public void setPcevDay28Date(String pcevDay28Date) {
        this.pcevDay28Date = pcevDay28Date;
    }

    public String getPcevDay28Batch() {
        return pcevDay28Batch;
    }

    public void setPcevDay28Batch(String pcevDay28Batch) {
        this.pcevDay28Batch = pcevDay28Batch;
    }

    public String getPcevOutcome() {
        return pcevOutcome;
    }

    public void setPcevOutcome(String pcevOutcome) {
        this.pcevOutcome = pcevOutcome;
    }

    public String getErig() {
        return erig;
    }

    public void setErig(String erig) {
        this.erig = erig;
    }

    public String getHrig() {
        return hrig;
    }

    public void setHrig(String hrig) {
        this.hrig = hrig;
    }

    public String getTetanusToxoidDate() {
        return tetanusToxoidDate;
    }

    public void setTetanusToxoidDate(String tetanusToxoidDate) {
        this.tetanusToxoidDate = tetanusToxoidDate;
    }

    public String getAtsDose() {
        return atsDose;
    }

    public void setAtsDose(String atsDose) {
        this.atsDose = atsDose;
    }

    public String getAtsDate() {
        return atsDate;
    }

    public void setAtsDate(String atsDate) {
        this.atsDate = atsDate;
    }

    public String getImpression() {
        return impression;
    }

    public void setImpression(String impression) {
        this.impression = impression;
    }

    @Override
    public String toString() {
        return "RabiesRecord{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", civilStatus='" + civilStatus + '\'' +
                ", address='" + address + '\'' +
                ", birthdate='" + birthdate + '\'' +
                ", birthPlace='" + birthPlace + '\'' +
                ", contactNo='" + contactNo + '\'' +
                ", philhealthNo='" + philhealthNo + '\'' +
                ", weightKg='" + weightKg + '\'' +
                ", bloodPressure='" + bloodPressure + '\'' +
                ", dateOfBite='" + dateOfBite + '\'' +
                ", timeOfBite='" + timeOfBite + '\'' +
                ", placeOfBite='" + placeOfBite + '\'' +
                ", injuryScratch=" + injuryScratch +
                ", injuryAbrasion=" + injuryAbrasion +
                ", injuryLaceration=" + injuryLaceration +
                ", injuryPunctured=" + injuryPunctured +
                ", injuryAvulsed=" + injuryAvulsed +
                ", injuryOthers=" + injuryOthers +
                ", injuryOthersSpecify='" + injuryOthersSpecify + '\'' +
                ", woundStatus='" + woundStatus + '\'' +
                ", woundWashing='" + woundWashing + '\'' +
                ", bitingAnimal='" + bitingAnimal + '\'' +
                ", bitingAnimalOthersSpecify='" + bitingAnimalOthersSpecify + '\'' +
                ", ownershipStatus='" + ownershipStatus + '\'' +
                ", animalStatusAtBite='" + animalStatusAtBite + '\'' +
                ", animalStatusAtConsult='" + animalStatusAtConsult + '\'' +
                ", animalDiedDate='" + animalDiedDate + '\'' +
                ", animalVaccination='" + animalVaccination + '\'' +
                ", animalVaccinationDate='" + animalVaccinationDate + '\'' +
                ", conditionEpilepsy=" + conditionEpilepsy +
                ", conditionDm=" + conditionDm +
                ", conditionHypertension=" + conditionHypertension +
                ", conditionAsthma=" + conditionAsthma +
                ", conditionAlcoholic=" + conditionAlcoholic +
                ", conditionEggAllergy=" + conditionEggAllergy +
                ", pvrvDay0Date='" + pvrvDay0Date + '\'' +
                ", pvrvDay0Batch='" + pvrvDay0Batch + '\'' +
                ", pvrvDay3Date='" + pvrvDay3Date + '\'' +
                ", pvrvDay3Batch='" + pvrvDay3Batch + '\'' +
                ", pvrvDay7Date='" + pvrvDay7Date + '\'' +
                ", pvrvDay7Batch='" + pvrvDay7Batch + '\'' +
                ", pvrvDay28Date='" + pvrvDay28Date + '\'' +
                ", pvrvDay28Batch='" + pvrvDay28Batch + '\'' +
                ", pvrvOutcome='" + pvrvOutcome + '\'' +
                ", pcevDay0Date='" + pcevDay0Date + '\'' +
                ", pcevDay0Batch='" + pcevDay0Batch + '\'' +
                ", pcevDay3Date='" + pcevDay3Date + '\'' +
                ", pcevDay3Batch='" + pcevDay3Batch + '\'' +
                ", pcevDay7Date='" + pcevDay7Date + '\'' +
                ", pcevDay7Batch='" + pcevDay7Batch + '\'' +
                ", pcevDay28Date='" + pcevDay28Date + '\'' +
                ", pcevDay28Batch='" + pcevDay28Batch + '\'' +
                ", pcevOutcome='" + pcevOutcome + '\'' +
                ", erig='" + erig + '\'' +
                ", hrig='" + hrig + '\'' +
                ", tetanusToxoidDate='" + tetanusToxoidDate + '\'' +
                ", atsDose='" + atsDose + '\'' +
                ", atsDate='" + atsDate + '\'' +
                ", impression='" + impression + '\'' +
                '}';
    }
}