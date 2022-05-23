val MIDI_TYPE_MAX:UShort = 0x3u
val MSB_TEST_VALUE:UByte = 0x80u //We need to see if the first bit is set - if it is, we read the next/following byte
val MSB_REST_VALUE:UByte = 0x7Fu //We & with this value to clear the leading bit of our 7 byte values
val END_OF_TRACK:UInt = 0xFF2F00u
val META_EVENT_IDENFIFIER:UByte = 0xFFu
val MIDI_TRK_VALUE:UInt = 0x4D54726Bu //This is MTrk

data class PrecussionKeyMap(val keyNum:UByte, val noteMapping:MidiNote, val precussionName:MidiPrecussionMap)

enum class MidiType {
    SINGLE, //Type 0
    SIMULTANEOUS, //Type 1
    SEQUENTIAL //Type 2
}

enum class TimingInfo {
    COMMON,
    CUT,
    THREE_TWO,
    FOUR_TWO,
    FIVE_TWO,
    SIX_TWO,
    SEVEN_TWO,
    EIGHT_TWO,
    NINE_TWO,
    TWO_FOUR,
    THREE_FOUR,
    FIVE_FOUR,
    SIX_FOUR,
    SEVEN_FOUR,
    EIGHT_FOUR,
    NINE_FOUR,
    TWO_EIGHT,
    THREE_EIGHT,
    FOUR_EIGHT,
    FIVE_EIGHT,
    SIX_EIGHT,
    SEVEN_EIGHT,
    EIGHT_EIGHT,
    NINE_EIGHT,
    TWO_SIXTEEN,
    THREE_SIXTEEN,
    FOUR_SIXTEEN,
    FIVE_SIXTEEN,
    SIX_SIXTEEN,
    SEVEN_SIXTEEN,
    EIGHT_SIXTEEN,
    NINE_SIXTEEN
}

enum class MetaEventDefinitions(val num:UShort) {
    UNKNOWN(0x0000u),
    SEQUENCE_NUMBER(0xFF00u), //Will be followed by 02 then the sequence number
    TEXT_INFO(0xFF01u), //Followed by LEN, TEXT. NOTE: The 0xFF01 - 0xFF0F are all reserved for text messages.
    COPYRIGHT(0xFF02u),
    TEXT_SEQUENCE(0xFF03u),
    TEXT_INSTRUMENT(0xFF04u),
    TEXT_LYRIC(0xFF05u),
    TEXT_MARKER(0xFF06u),
    TEXT_CUE_POINT(0xFF07u),
    MIDI_CHANNEL(0xFF20u),
    PORT_SELECTION(0xFF21u), //Also has a 01 after the 21, and then a byte (0 - 127) Identifing the port number.
    END_OF_TRACK(0xFF2Fu),
    TEMPO(0xFF51u),
    SMPTE(0xFF54u),
    TIME_SIGNATURE(0xFF58u),
    KEY_SIGNATURE(0xFF59u),
    SPECIAL_SEQUENCE(0xFF7Fu)
}

enum class TrackEventType {
    MIDI_EVENT,
    SYSEX_EVENT,
    SYSREALTIME_EVENT,
    META_EVENT
}

//IF - your mode control message implements mode control - which is identified if the first of the two
//bytes following the status message has a value of 122, 123, 124, 125, 126, or 127 then you'll use both
//the control number in combination with the new value to be in one of the following control modes
enum class MidiEventModeControlStates {
    UNDEFINED,
    LOCAL_CONTROL_ON,
    LOCAL_CONTROL_OFF,
    ALL_NOTES_OFF,
    OMNI_MODE_OFF,
    OMNI_MODE_ON,
    MONO_MODE_ON,
    MONO_MODE_OFF
}

//Section 1.1 - Major Midi messages defined
enum class MidiMajorMessage(val num:UByte) {
    UNDEFINED(0x00u),

    //MidiChannel Voice/Mode Message - Commonly refered to as simply channel messages
    NOTE_OFF(0x80u),
    NOTE_ON(0x90u),
    KEY_PRESSURE_AFTER_TOUCH(0xA0u),
    CONTROL_CHANGE(0xB0u), //There is particular rules to follow to interpret this message
    PROGRAM_CHANGE(0xC0u),
    CHANNEL_PRESSURE_AFTER_TOUCH(0xD0u),
    PITCH_WHEEL_CHANGE(0xE0u),

    //Section 1.1 - Third messages defined: MidiSystemCommonMessages
    SYS_EXCLUSIVE(0xF0u),
    SONG_POSITION_POINTER(0xF2u),
    SONG_SELECT(0xF3u),
    TUNE_REQUEST(0xF6u),
    END_OF_EXCLUSIVE(0xF7u),

    //Section 1.1 - Last messages defined - MidiSystemRealTimeMessage
    TIMING_CLOCK(0xF8u),
    START_SEQUENCE(0xFAu),
    CONTINUE_AT_POINT_OF_SEQUENCE_STOP(0xFBu),
    STOP_SEQUENCE(0xFCu),
    ACTIVE_SENSING(0xFEu),
    RESET(0xFFu)
}

//From Table 1.2
//These are the 2nd byte message identifier values - NOT the actual 3rd byte values
//https://midimusic.github.io/tech/midispec.html#BMA1_
//
//MidiControllerMessage.values().forEach { println(it) }
enum class MidiControllerMessage(val num:UByte) {
    BANK_SELECT(0x00u),
    MODULATION_WHEEL(0x01u),
    BREATH_CONTROL(0x02u),
    FOOT_CONTROL(0x04u),
    PORTAMENTO_TIME(0x05u),
    DATA_ENTRY(0x06u),
    CHANNEL_VOLUME(0x07u),
    BALANCE(0x08u),
    PAN(0x0Au),
    EXPRESSION_CONTROLLER(0x0Bu),
    EFFECT_CTRL_1(0x0Cu),
    EFFECT_CTRL_2(0x0Du),
    GEN_PURPOSE_CTRLR_1(0x10u),
    GEN_PURPOSE_CTRLR_2(0x11u),
    GEN_PURPOSE_CTRLR_3(0x12u),
    GEN_PURPOSE_CTRLR_4(0x13u),

    //There seems to be two values which apply to the same named messages
    BANK_SELECT_2nd(0x20u),
    MODULATION_WHEEL_2nd(0x21u),
    BREATH_CONTROL_2nd(0x22u),
    FOOT_CONTROL_2nd(0x24u),
    PORTAMENTO_TIME_2nd(0x25u),
    DATA_ENTRY_2nd(0x26u),
    CHANNEL_VOLUME_2nd(0x27u),
    BALANCE_2nd(0x28u),
    PAN_2nd(0x2Au),
    EXPRESSION_CONTROLLER_2nd(0x2Bu),
    EFFECT_CTRL_1_2nd(0x2Cu),
    EFFECT_CTRL_2_2nd(0x2Du),
    GEN_PURPOSE_CTRLR_1_2nd(0x30u),
    GEN_PURPOSE_CTRLR_2_2nd(0x31u),
    GEN_PURPOSE_CTRLR_3_2nd(0x32u),
    GEN_PURPOSE_CTRLR_4_2nd(0x33u),

    DAMPER_PEDAL_ON_OFF_SUSTAIN(0x40u),
    PORTAMENTO_ON_OFF(0x41u),
    SUSTENUTO_ON_OFF(0x42u),
    SOFT_PEDEL_ON_OFF(0x43u),
    LEGATO_FOOTSWITCH(0x44u),
    HOLD_2(0x45u),
    SOUND_CTRLR_1(0x46u),
    SOUND_CTRLR_2(0x47u),
    SOUND_CTRLR_3(0x48u),
    SOUND_CTRLR_4(0x49u),
    SOUND_CTRLR_5(0x4Au),
    SOUND_CTRLR_6(0x4Bu),
    SOUND_CTRLR_7(0x4Cu),
    SOUND_CTRLR_8(0x4Du),
    SOUND_CTRLR_9(0x4Eu),
    SOUND_CTRLR_10(0x4Fu),

    GEN_PURPOSE_CTRLR_5(0x50u),
    GEN_PURPOSE_CTRLR_6(0x51u),
    GEN_PURPOSE_CTRLR_7(0x52u),
    GEN_PURPOSE_CTRLR_8(0x53u),
    PORTAMENTO_CTRL(0x54u),
    EFFECTS_1_DEPTH(0x5Bu),
    EFFECTS_2_DEPTH(0x5Cu),
    EFFECTS_3_DEPTH(0x5Du),
    EFFECTS_4_DEPTH(0x5Eu),
    EFFECTS_5_DEPTH(0x5Fu),

    DATA_ENTRY_ADD_1(0x60u),
    DATA_ENTRY_SUB_1(0x61u),

    //Note: The 2 sets of pairings are probably going to use the 7 bits formatting to combine values
    NON_REG_PARAM_NUM_LSB(0x62u),
    NON_REG_PARAM_NUM_MSB(0x63u),

    REG_PARAM_NUM_LSB(0x64u),
    REG_PARAM_NUM_MSB(0x65u),

    ALL_SOUND_OFF(0x78u),
    RESET_ALL_CONTROLLERS(0x79u),
    LOCAL_CONTROLLER_ON_OFF(0x7Au),
    ALL_NOTES_OFF(0x7Bu),
    OMNI_MODE_OFF(0x7Cu),
    OMNI_MODE_OB(0x7Du),
    POLY_MODE_ON_OFF(0x7Eu),
    POLY_MODE_ON(0x7u)
}

//From Table 1.3
//Piano Octave 0 - 10 each have all 12 notes.
//The user will need to determin if they want to
//evaluate the note as a sharp or flat based on the key
//as there are no values provided for flattened notes
//
//val Notes = enumValues<MidiNotes>()
//With the above you can easily iterate over all values:
//
//enumValues<MidiNotes>().forEach { println(it.name) }
//To map enum name to enum value use valueOf/enumValueOf like so:
//
//val centerC = MidiNote.valueOf("C")
//val DNote = enumValueOf<MidiNote>("D")
//
//Alternative:
//MidiNotes.values().forEach { println(it) }
enum class MidiNote(val num:UByte) {
    UNDEFINED(0xFFu),
    OCTAVE_ZERO_C(0x0u),
    OCTAVE_ZERO_C_SHRP(0x1u), //D_Flat
    OCTAVE_ZERO_D(0x2u),
    OCTAVE_ZERO_D_SHRP(0x3u), //E_FLAT
    OCTAVE_ZERO_E(0x4u),
    OCTAVE_ZERO_F(0x5u), //E_SHARP
    OCTAVE_ZERO_F_SHRP(0x6u), //G_FLAT
    OCTAVE_ZERO_G(0x7u),
    OCTAVE_ZERO_G_SHRP(0x8u), //A_FLAT
    OCTAVE_ZERO_A(0x9u),
    OCTAVE_ZERO_A_SHARP(0xAu), //B_FLAT
    OCTAVE_ZERO_B(0xBu), //C_FLAT

    OCTAVE_ONE_C(0xCu),
    OCTAVE_ONE_C_SHRP(0xDu),
    OCTAVE_ONE_D(0xEu),
    OCTAVE_ONE_D_SHRP(0xFu),
    OCTAVE_ONE_E(0x10u),
    OCTAVE_ONE_F(0x11u),
    OCTAVE_ONE_F_SHRP(0x12u),
    OCTAVE_ONE_G(0x13u),
    OCTAVE_ONE_G_SHRP(0x14u),
    OCTAVE_ONE_A(0x15u),
    OCTAVE_ONE_A_SHARP(0x16u),
    OCTAVE_ONE_B(0x17u),

    OCTAVE_TWO_C(0x18u),
    OCTAVE_TWO_C_SHRP(0x19u),
    OCTAVE_TWO_D(0x1Au),
    OCTAVE_TWO_D_SHRP(0x1Bu),
    OCTAVE_TWO_E(0x1Cu),
    OCTAVE_TWO_F(0x1Du),
    OCTAVE_TWO_F_SHRP(0x1Eu),
    OCTAVE_TWO_G(0x1Fu),
    OCTAVE_TWO_G_SHRP(0x20u),
    OCTAVE_TWO_A(0x21u),
    OCTAVE_TWO_A_SHARP(0x22u),
    OCTAVE_TWO_B(0x23u),

    OCTAVE_THREE_C(0x24u),
    OCTAVE_THREE_C_SHRP(0x25u),
    OCTAVE_THREE_D(0x26u),
    OCTAVE_THREE_D_SHRP(0x27u),
    OCTAVE_THREE_E(0x28u),
    OCTAVE_THREE_F(0x29u),
    OCTAVE_THREE_F_SHRP(0x2Au),
    OCTAVE_THREE_G(0x2Bu),
    OCTAVE_THREE_G_SHRP(0x2Cu),
    OCTAVE_THREE_A(0x2Du),
    OCTAVE_THREE_A_SHARP(0x2Eu),
    OCTAVE_THREE_B(0x2Fu),

    OCTAVE_FOUR_C(0x30u),
    OCTAVE_FOUR_C_SHRP(0x31u),
    OCTAVE_FOUR_D(0x32u),
    OCTAVE_FOUR_D_SHRP(0x33u),
    OCTAVE_FOUR_E(0x34u),
    OCTAVE_FOUR_F(0x35u),
    OCTAVE_FOUR_F_SHRP(0x36u),
    OCTAVE_FOUR_G(0x37u),
    OCTAVE_FOUR_G_SHRP(0x38u),
    OCTAVE_FOUR_A(0x39u),
    OCTAVE_FOUR_A_SHARP(0x3Au),
    OCTAVE_FOUR_B(0x3Bu),

    OCTAVE_FIVE_C(0x3Cu),
    OCTAVE_FIVE_C_SHRP(0x3Du),
    OCTAVE_FIVE_D(0x3Eu),
    OCTAVE_FIVE_D_SHRP(0x3Fu),
    OCTAVE_FIVE_E(0x40u),
    OCTAVE_FIVE_F(0x41u),
    OCTAVE_FIVE_F_SHRP(0x42u),
    OCTAVE_FIVE_G(0x43u),
    OCTAVE_FIVE_G_SHRP(0x44u),
    OCTAVE_FIVE_A(0x45u),
    OCTAVE_FIVE_A_SHARP(0x46u),
    OCTAVE_FIVE_B(0x47u),

    OCTAVE_SIX_C(0x48u),
    OCTAVE_SIX_C_SHRP(0x49u),
    OCTAVE_SIX_D(0x4Au),
    OCTAVE_SIX_D_SHRP(0x4Bu),
    OCTAVE_SIX_E(0x4Cu),
    OCTAVE_SIX_F(0x4Du),
    OCTAVE_SIX_F_SHRP(0x4Eu),
    OCTAVE_SIX_G(0x4Fu),
    OCTAVE_SIX_G_SHRP(0x50u),
    OCTAVE_SIX_A(0x51u),
    OCTAVE_SIX_A_SHARP(0x52u),
    OCTAVE_SIX_B(0x53u),

    OCTAVE_SEVEN_C(0x54u),
    OCTAVE_SEVEN_C_SHRP(0x55u),
    OCTAVE_SEVEN_D(0x56u),
    OCTAVE_SEVEN_D_SHRP(0x57u),
    OCTAVE_SEVEN_E(0x58u),
    OCTAVE_SEVEN_F(0x59u),
    OCTAVE_SEVEN_F_SHRP(0x5Au),
    OCTAVE_SEVEN_G(0x5Bu),
    OCTAVE_SEVEN_G_SHRP(0x5Cu),
    OCTAVE_SEVEN_A(0x5Du),
    OCTAVE_SEVEN_A_SHARP(0x5Eu),
    OCTAVE_SEVEN_B(0x5Fu),

    OCTAVE_EIGHT_C(0x60u),
    OCTAVE_EIGHT_C_SHRP(0x61u),
    OCTAVE_EIGHT_D(0x62u),
    OCTAVE_EIGHT_D_SHRP(0x63u),
    OCTAVE_EIGHT_E(0x64u),
    OCTAVE_EIGHT_F(0x65u),
    OCTAVE_EIGHT_F_SHRP(0x66u),
    OCTAVE_EIGHT_G(0x67u),
    OCTAVE_EIGHT_G_SHRP(0x68u),
    OCTAVE_EIGHT_A(0x69u),
    OCTAVE_EIGHT_A_SHARP(0x6Au),
    OCTAVE_EIGHT_B(0x6Bu),

    OCTAVE_NINE_C(0x6Cu),
    OCTAVE_NINE_C_SHRP(0x6Du),
    OCTAVE_NINE_D(0x6Eu),
    OCTAVE_NINE_D_SHRP(0x6Fu),
    OCTAVE_NINE_E(0x70u),
    OCTAVE_NINE_F(0x71u),
    OCTAVE_NINE_F_SHRP(0x72u),
    OCTAVE_NINE_G(0x73u),
    OCTAVE_NINE_G_SHRP(0x74u),
    OCTAVE_NINE_A(0x75u),
    OCTAVE_NINE_A_SHARP(0x76u),
    OCTAVE_NINE_B(0x77u),

    OCTAVE_TEN_C(0x78u),
    OCTAVE_TEN_C_SHRP(0x79u),
    OCTAVE_TEN_D(0x7Au),
    OCTAVE_TEN_D_SHRP(0x7Bu),
    OCTAVE_TEN_E(0x7Cu),
    OCTAVE_TEN_F(0x7Du),
    OCTAVE_TEN_F_SHRP(0x7Eu),
    OCTAVE_TEN_G(0x7Fu); //Last piano key

	companion object {
		fun convertByteToNote(value: UByte) : MidiNote
		{
			for (e in enumValues<MidiNote>())
			{
				if(e.num == value)
					return e
			}

			return MidiNote.UNDEFINED
		}
	}
}

enum class MusicalKey {
	UNKNOWN,
	//Circle of 5ths. Major and relative minor keys.
    C_MAJ,
    A_MIN,
    G_MAJ,
    E_MIN,
    D_MAJ,
    B_MIN,
    A_MAJ,
    FSHRP_MIN,
    E_MAJ,
    CSHRP_MIN,
    B_MAJ,
    GSHRP_MIN,
    FSHRP_MAJ,
    DSHRP_MIN,
    CSHRP_MAJ,
    ASHRP_MIN,

    //Circle of 4ths. Major and relative minor keys
    F_MAJ,
    D_MIN,
    BFLAT_MAJ,
    G_MIN,
    EFLAT_MAJ,
    C_MIN,
    AFLAT_MAJ,
    F_MIN,
    DFLAT_MAJ,
    BFLAT_MIN,
    GFLAT_MAJ,
    EFLAT_MIN,
    CFLAT_MAJ,
    AFLAT_MIN

//    static func musicalKeyToString(p:MusicalKey?) -> String
//    {
//        guard let k = p else {return ""}
//
//        var s:String = ""
//
//        switch(k) {
//            case MusicalKey.C_MAJ:
//            s = "C-Maj"
//            case MusicalKey.A_MIN:
//            s = "A-min"
//            case MusicalKey.G_MAJ:
//            s = "G-Maj"
//            case MusicalKey.E_MIN:
//            s = "E-min"
//            case MusicalKey.D_MAJ:
//            s = "D-Maj"
//            case MusicalKey.B_MIN:
//            s = "B-min"
//            case MusicalKey.A_MAJ:
//            s = "A-Maj"
//            case MusicalKey.FSHRP_MIN:
//            s = "F#-min"
//            case MusicalKey.E_MAJ:
//            s = "E-Maj"
//            case MusicalKey.CSHRP_MIN:
//            s = "C#-min"
//            case MusicalKey.B_MAJ:
//            s = "B-Maj"
//            case MusicalKey.GSHRP_MIN:
//            s = "G#-min"
//            case MusicalKey.FSHRP_MAJ:
//            s = "F#-Maj"
//            case MusicalKey.DSHRP_MIN:
//            s = "D#-min"
//            case MusicalKey.CSHRP_MAJ:
//            s = "C#-Maj"
//            case MusicalKey.ASHRP_MIN:
//            s = "A#-min"
//
//            /////////////////
//            case MusicalKey.F_MAJ:
//            s = "F-Maj"
//            case MusicalKey.D_MIN:
//            s = "D-min"
//            case MusicalKey.BFLAT_MAJ:
//            s = "Bb-Maj"
//            case MusicalKey.G_MIN:
//            s = "G-min"
//            case MusicalKey.EFLAT_MAJ:
//            s = "Eb-Maj"
//            case MusicalKey.C_MIN:
//            s = "C-min"
//            case MusicalKey.AFLAT_MAJ:
//            s = "Ab-Maj"
//            case MusicalKey.F_MIN:
//            s = "F-min"
//            case MusicalKey.DFLAT_MAJ:
//            s = "Db-Maj"
//            case MusicalKey.BFLAT_MIN:
//            s = "Bb-min"
//            case MusicalKey.GFLAT_MAJ:
//            s = "Gb-Maj"
//            case MusicalKey.EFLAT_MIN:
//            s = "Eb-min"
//            case MusicalKey.CFLAT_MAJ:
//            s = "Cb-Maj"
//            case MusicalKey.AFLAT_MIN:
//            s = "Ab-min"
//        }
//
//        return s
//    }
}

//From Appendix 1.4 table 1
enum class MidiInstrumentGeneralFamily {
	INST_FAMILY_PIANO,
	INST_FAMILY_CHROMATIC_PRECUSSION,
    INST_FAMILY_ORGAN,
    INST_FAMILY_GUITAR,
    INST_FAMILY_BASS,
    INST_FAMILY_STRINGS,
    INST_FAMILY_ENSENBLE,
    INST_FAMILY_BRASS,
    INST_FAMILY_REED,
    INST_FAMILY_PIPE,
    INST_FAMILY_SYNTH_LEAD,
    INST_FAMILY_SYNTH_PAD,
    INST_FAMILY_SYNTH_EFFECTS,
    INST_FAMILY_ETHNIC,
    INST_FAMILY_PERCUSSIVE,
    INST_FAMILY_SOUND_EFFECTS
}

//From Appendix 1.4 table 2
//This is essentially, additional/extra sound information for a given synth sound
enum class MidiInstrumentPatch {
    ACOUSTIC_GRAND_PIANO,
    BRIGHT_ACOUSTIC_PIANO,
    ELECTRIC_GRAND_PIANO,
    HONKY_TONK_PIANO,
    ELECTRIC_PIANO_1_RHODES_PIANO,
    ELECTRIC_PIANO_2_CHORUSED_PIANO,
    HARPSICHORD,
    CLAVINET,
    CELESTA,
    GLOCKENSPIEL,
    MUSIC_BOX,
    VIBRAPHONE,
    MARIMBA,
    XYLOPHONE,
    TUBULAR_BELLS,
    DULCIMER_SANTUR,
    DRAWBAR_ORGAN_HAMMOND,
    PERCUSSIVE_ORGAN,
    ROCK_ORGAN,
    CHURCH_ORGAN,
    REED_ORGAN,
    ACCORDION_FRENCH,
    HARMONICA,
    TANGO_ACCORDION_BAND_NEON,
    ACOUSTIC_GUITAR_NYLON,
    ACOUSTIC_GUITAR_STEEL,
    ELECTRIC_GUITAR_JAZZ,
    ELECTRIC_GUITAR_CLEAN,
    ELECTRIC_GUITAR_MUTED,
    OVERDRIVEN_GUITAR,
    DISTORTION_GUITAR,
    GUITAR_HARMONICS,
    ACOUSTIC_BASS,
    ELECTRIC_BASS_FINGERED,
    ELECTRIC_BASS_PICKED,
    FRETLESS_BASS,
    SLAP_BASS_1,
    SLAP_BASS_2,
    SYNTH_BASS_1,
    SYNTH_BASS_2,
    VIOLIN,
    VIOLA,
    CELLO,
    CONTRABASS,
    TREMOLO_STRINGS,
    PIZZICATO_STRINGS,
    ORCHESTRAL_HARP,
    TIMPANI,
    STRING_ENSEMBLE_1_STRINGS,
    STRING_ENSEMBLE_2_SLOW_STRINGS,
    SYNTHSTRINGS_1,
    SYNTHSTRINGS_2,
    CHOIR_AAHS,
    VOICE_OOHS,
    SYNTH_VOICE,
    ORCHESTRA_HIT,
    TRUMPET,
    TROMBONE,
    TUBA,
    MUTED_TRUMPET,
    FRENCH_HORN,
    BRASS_SECTION,
    SYNTHBRASS_1,
    SYNTHBRASS_2,
    SOPRANO_SAX,
    ALTO_SAX,
    TENOR_SAX,
    BARITONE_SAX,
    OBOE,
    ENGLISH_HORN,
    BASSOON,
    CLARINET,
    PICCOLO,
    FLUTE,
    RECORDER,
    PAN_FLUTE,
    BLOWN_BOTTLE,
    SHAKUHACHI,
    WHISTLE,
    OCARINA,
    LEAD_1_SQUARE_WAVE,
    LEAD_2_SAWTOOTH_WAVE,
    LEAD_3_CALLIOPE,
    LEAD_4_CHIFFER,
    LEAD_5_CHARANG,
    LEAD_6_VOICE_SOLO,
    LEAD_7_FIFTHS,
    LEAD_8_BASS_LEAD,
    PAD_1_NEW_AGE_FANTASIA,
    PAD_2_WARM,
    PAD_3_POLYSYNTH,
    PAD_4_CHOIR_SPACE_VOICE,
    PAD_5_BOWED_GLASS,
    PAD_6_METALLIC_PRO,
    PAD_7_HALO,
    PAD_8_SWEEP,
    FX_1_RAIN,
    FX_2_SOUNDTRACK,
    FX_3_CRYSTAL,
    FX_4_ATMOSPHERE,
    FX_5_BRIGHTNESS,
    FX_6_GOBLINS,
    FX_7_ECHOES_DROPS,
    FX_8_SCI_FI_STAR_THEME,
    SITAR,
    BANJO,
    SHAMISEN,
    KOTO,
    KALIMBA,
    BAG_PIPE,
    FIDDLE,
    SHANAI,
    TINKLE_BELL,
    AGOGO,
    STEEL_DRUMS,
    WOODBLOCK,
    TAIKO_DRUM,
    MELODIC_TOM,
    SYNTH_DRUM,
    REVERSE_CYMBAL,
    GUITAR_FRET_NOISE,
    BREATH_NOISE,
    SEASHORE,
    BIRD_TWEET,
    TELEPHONE_RING,
    HELICOPTER,
    APPLAUSE,
    GUNSHOT
}

enum class MidiPrecussionMap {
    ACOUSTIC_BASS_DRUM,
    BASS_DRUM_1,
    SIDE_STICK,
    ACOUSTIC_SNARE,
    HAND_CLAP,
    ELECTRIC_SNARE,
    LOW_FLOOR_TOM,
    CLOSED_HI_HAT,
    HIGH_FLOOR_TOM,
    PEDAL_HI_HAT,
    LOW_TOM,
    OPEN_HI_HAT,
    LOW_MID_TOM,
    HI_MID_TOM,
    CRASH_CYMBAL_1,
    HIGH_TOM,
    RIDE_CYMBAL_1,
    CHINESE_CYMBAL,
    RIDE_BELL,
    TAMBOURINE,
    SPLASH_CYMBAL,
    COWBELL,
    CRASH_CYMBAL_2,
    VIBRASLAP,
    RIDE_CYMBAL_2,
    HI_BONGO,
    LOW_BONGO,
    MUTE_HI_CONGA,
    OPEN_HI_CONGA,
    LOW_CONGA,
    HIGH_TIMBALE,
    LOW_TIMBALE,
    HIGH_AGOGO,
    LOW_AGOGO,
    CABASA,
    MARACAS,
    SHORT_WHISTLE,
    LONG_WHISTLE,
    SHORT_GUIRO,
    LONG_GUIRO,
    CLAVES,
    HI_WOOD_BLOCK,
    LOW_WOOD_BLOCK,
    MUTE_CUICA,
    OPEN_CUICA,
    MUTE_TRIANGLE,
    OPEN_TRIANGLE
}