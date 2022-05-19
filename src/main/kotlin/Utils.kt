import jdk.jfr.Unsigned
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.*

class Utils
{
	companion object {
		fun byteToAphaNumericChar(byte:UByte) : Pair<Boolean, Char?>//(isPrintable:Boolean, theChar:UByte)
		{
			val PRINTABLE_ASCII_NUMBER_RANGE_MIN:UByte = 0x30u
			val PRINTABLE_ASCII_NUMBER_RANGE_MAX:UByte = 0x39u

			val PRINTABLE_ASCII_UPPER_LETTER_RANGE_MIN:UByte = 0x41u
			val PRINTABLE_ASCII_UPPER_LETTER_RANGE_MAX:UByte = 0x5Au

			val PRINTABLE_ASCII_LOWER_LETTER_RANGE_MIN:UByte = 0x61u
			val PRINTABLE_ASCII_LOWER_LETTER_RANGE_MAX:UByte = 0x7Au

			var c:Char? = null
			var isChar:Boolean = false

			when(byte) {
				in PRINTABLE_ASCII_NUMBER_RANGE_MIN..PRINTABLE_ASCII_NUMBER_RANGE_MAX -> {
					isChar = true
					c = Char(byte.toInt())
				}

				in PRINTABLE_ASCII_LOWER_LETTER_RANGE_MIN..PRINTABLE_ASCII_LOWER_LETTER_RANGE_MAX -> {
					isChar = true
					c = Char(byte.toInt()).lowercaseChar()
				}

				in PRINTABLE_ASCII_UPPER_LETTER_RANGE_MIN..PRINTABLE_ASCII_UPPER_LETTER_RANGE_MAX -> {
					isChar = true
					c = Char(byte.toInt()).uppercaseChar()
				}
			}

			return Pair<Boolean, Char?> (isChar, c)  //((c.isLetter || c.isNumber), c)
		}

		fun into16Bit(byte1:UByte, byte2:UByte) : UShort
		{
			var X:UShort

			var X1:UInt = byte1.toUInt()  //  UShort(byte1)
			X1 = X1 shl 8
			X = (X1 + byte2.toUInt()).toUShort()

			Printer.printUInt16AsHex(X)
			Printer.printByteValuesAsHex(byte1, byte2, numBytes = 2)
			Printer.printByteValuesAsBinary(byte1, byte2, numBytes = 2)
			Printer.printByteValuesAsDecimal(byte1, byte2, numBytes = 2)

			return X
		}

		fun into32Bit(byte1:UByte, byte2:UByte, byte3:UByte, byte4:UByte) : UInt
		{
			var X:UInt

			var X1:UInt = byte1.toUInt()
			X1 = X1 shl 24

			var X2:UInt = byte2.toUInt()
			X2 = X2 shl 16

			var X3:UInt = byte3.toUInt()
			X3 = X3 shl 8

			X = X1 + X2 + X3 + byte4.toUInt()

			Printer.printUInt32AsHex(X)
			Printer.printByteValuesAsHex(byte1, byte2, byte3, byte4)
			Printer.printByteValuesAsBinary(byte1, byte2, byte3, byte4)
			Printer.printByteValuesAsDecimal(byte1, byte2, byte3, byte4)

			return X
		}

		//I think this really will only ever be a UInt32 - MAX!
		//The pais returned is the new index followed by the variables length bytes read
		fun readVariableLengthValue(startIdx:AtomicInteger, data:ByteArray) : UInt?
		{
			if( (startIdx.get() <= 0) || (startIdx.get() >= data.size) ) return null

			var numberOfBytesRead:Int = 0 //We know we're going to read at least one byte

			var totalValue:UInt = 0u
			var valueAtByteValue:UByte

			do {
				valueAtByteValue = data[startIdx.get()].toUByte()

				//You MUST clear out the leading bit before adding it to our deltatime value! Just simply
				//ALWAYS doing it since it does no harm regardless if the leading bit is 0 or 1
				//but keeps the logic and readability clean
				val deltaTimeValueToAdd:UByte = valueAtByteValue and MSB_REST_VALUE //Need to use a tmp variable so the loop predicate still works correctly

				totalValue = totalValue shl (numberOfBytesRead * 8)//First shift our stored value. 8 because it is the size of a byte
				totalValue += deltaTimeValueToAdd.toUInt()

				startIdx.incrementAndGet()
				numberOfBytesRead += 1
			}while ((valueAtByteValue and MSB_TEST_VALUE) == MSB_TEST_VALUE)

			return totalValue
		}

		fun timeSignatureFromNumeratorDenominator(numerator:UByte, denominator:UByte) : TimingInfo
		{
			val timing:TimingInfo = TimingInfo.COMMON
			val V:Double = 2.0

			//First get the denominator to a base 10 non-decimal value. if: Denominator may come is as 3
			//Which means 2^-3 - this is because it's supposed to denote 2/8 for example
			val D:UByte = ((V.pow(denominator.toDouble())).toUInt()).toUByte()  //2^denominator

			if( (D.toUInt() == 0u) &&
				((D.toUInt() and (D.toUInt() - 1u)) == 0u) )
			{
				return TimingInfo.COMMON
			}

			if( (numerator.toUInt() == 2u) && (D.toUInt() == 2u) )
			{
				return TimingInfo.CUT
			}
			else if( (numerator.toUInt() == 4u) && (D.toUInt() == 4u) ) {
				return TimingInfo.COMMON
			}

			if(numerator.toUInt() == 2u) {
				when(D.toUInt()) {
					4u ->
						return TimingInfo.TWO_FOUR
					8u ->
						return TimingInfo.TWO_EIGHT
					16u ->
						return TimingInfo.TWO_SIXTEEN
					else ->
						return TimingInfo.CUT
				}
			}
			else if(numerator.toUInt() == 3u) {
				when(D.toUInt()) {
					2u ->
						return TimingInfo.THREE_TWO
					4u ->
						return TimingInfo.THREE_FOUR
					8u ->
						return TimingInfo.THREE_EIGHT
					16u ->
						return TimingInfo.THREE_SIXTEEN
					else ->
						return TimingInfo.COMMON
				}
			}
			else if(numerator.toUInt() == 4u) {
				when(D.toUInt()) {
					2u ->
						return TimingInfo.FOUR_TWO
					8u ->
						return TimingInfo.FOUR_EIGHT
					16u ->
						return TimingInfo.FOUR_SIXTEEN
					else ->
						return TimingInfo.COMMON
				}
			}
			else if(numerator.toUInt() == 5u) {
				when(D.toUInt()) {
					2u ->
						return TimingInfo.FIVE_TWO
					4u ->
						return TimingInfo.FIVE_FOUR
					8u ->
						return TimingInfo.FIVE_EIGHT
					16u ->
						return TimingInfo.FIVE_SIXTEEN
					else ->
						return TimingInfo.COMMON
				}
			}
			else if(numerator.toUInt() == 6u) {
				when(D.toUInt()) {
					2u ->
						return TimingInfo.SIX_TWO
					4u ->
						return TimingInfo.SIX_FOUR
					8u ->
						return TimingInfo.SIX_EIGHT
					16u ->
						return TimingInfo.SIX_SIXTEEN
					else ->
						return TimingInfo.COMMON
				}
			}
			else if(numerator.toUInt() == 7u) {
				when(D.toUInt()) {
					2u ->
						return TimingInfo.SEVEN_TWO
					4u ->
						return TimingInfo.SEVEN_FOUR
					8u ->
						return TimingInfo.SEVEN_EIGHT
					16u ->
						return TimingInfo.SEVEN_SIXTEEN
					else ->
						return TimingInfo.COMMON
				}
			}
			else if(numerator.toUInt() == 8u) {
				when(D.toUInt()) {
					2u ->
						return TimingInfo.EIGHT_TWO
					4u ->
						return TimingInfo.EIGHT_FOUR
					8u ->
						return TimingInfo.EIGHT_EIGHT
					16u ->
						return TimingInfo.EIGHT_SIXTEEN
					else ->
						return TimingInfo.COMMON
				}
			}
			else if(numerator.toUInt() == 9u) {
				when(D.toUInt()) {
					2u ->
						return TimingInfo.NINE_TWO
					4u ->
						return TimingInfo.NINE_FOUR
					8u ->
						return TimingInfo.NINE_EIGHT
					16u ->
						return TimingInfo.NINE_SIXTEEN
					else ->
						return TimingInfo.COMMON
				}
			}

			return timing
	}

		//IMPORTANT!!!!! Channel value of 0 is refered to as channel 1. Ie: it's zero index based like an array [1, 2, 3, 4...]
		//Channel value at index 0 is 1
		//fun valueToChannelVoiceMessage(messageValue:UByte) -> (msg:MidiMajorMessage, channel:UByte)
		fun valueToChannelVoiceMessage(messageValue:UByte) : Pair<MidiMajorMessage, UByte> //(msg:MidiMajorMessage, channel:UByte)
		{
			var voiceMsg:MidiMajorMessage
			val channelCtrl:UByte = 0x0Fu
			val chnl:UByte = messageValue and channelCtrl

			if( (messageValue and MidiMajorMessage.NOTE_OFF.num) == MidiMajorMessage.NOTE_OFF.num) {
				voiceMsg = MidiMajorMessage.NOTE_OFF}
			else if( (messageValue and MidiMajorMessage.NOTE_ON.num) == MidiMajorMessage.NOTE_ON.num) {
				voiceMsg = MidiMajorMessage.NOTE_ON}
			else if( (messageValue and MidiMajorMessage.KEY_PRESSURE_AFTER_TOUCH.num) == MidiMajorMessage.KEY_PRESSURE_AFTER_TOUCH.num) {
				voiceMsg = MidiMajorMessage.KEY_PRESSURE_AFTER_TOUCH}
			else if( (messageValue and MidiMajorMessage.CONTROL_CHANGE.num) == MidiMajorMessage.CONTROL_CHANGE.num) {
				voiceMsg = MidiMajorMessage.CONTROL_CHANGE}
			else if( (messageValue and MidiMajorMessage.PROGRAM_CHANGE.num) == MidiMajorMessage.PROGRAM_CHANGE.num) {
				voiceMsg = MidiMajorMessage.PROGRAM_CHANGE}
			else if( (messageValue and MidiMajorMessage.CHANNEL_PRESSURE_AFTER_TOUCH.num) == MidiMajorMessage.CHANNEL_PRESSURE_AFTER_TOUCH.num) {
				voiceMsg = MidiMajorMessage.CHANNEL_PRESSURE_AFTER_TOUCH}
			else if( (messageValue and MidiMajorMessage.PITCH_WHEEL_CHANGE.num) == MidiMajorMessage.PITCH_WHEEL_CHANGE.num) {
				voiceMsg = MidiMajorMessage.PITCH_WHEEL_CHANGE}
			else
				voiceMsg = MidiMajorMessage.UNDEFINED

			return Pair<MidiMajorMessage, UByte>(voiceMsg, chnl)
		}

		fun valueToGeneralFamily(familyValue:UByte) : MidiInstrumentGeneralFamily
		{
			when(familyValue.toInt()) {
				in 1..8 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_PIANO
				in 9..16 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_CHROMATIC_PRECUSSION
				in 17..24 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_ORGAN
				in 25..32 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_GUITAR
				in 33..40 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_BASS
				in 41..48 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_STRINGS
				in 49..56 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_ENSENBLE
				in 57..64 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_BRASS
				in 65..72 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_REED
				in 73..80 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_PIPE
				in 81..88 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_SYNTH_LEAD
				in 89..96 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_SYNTH_PAD
				in 97..104 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_SYNTH_EFFECTS
				in 105..112 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_ETHNIC
				in 113..120 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_PERCUSSIVE
				in 121..128 ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_SOUND_EFFECTS
				else ->
					return MidiInstrumentGeneralFamily.INST_FAMILY_PIANO
			}//End switch
		}

		fun valueToInstrumentPatch(patchValue:UShort) : MidiInstrumentPatch
		{
			when(patchValue.toUInt()) {
				1u ->
					return MidiInstrumentPatch.ACOUSTIC_GRAND_PIANO
				2u ->
					return MidiInstrumentPatch.BRIGHT_ACOUSTIC_PIANO
				3u ->
					return MidiInstrumentPatch.ELECTRIC_GRAND_PIANO
				4u ->
					return MidiInstrumentPatch.HONKY_TONK_PIANO
				5u ->
					return MidiInstrumentPatch.ELECTRIC_PIANO_1_RHODES_PIANO
				6u ->
					return MidiInstrumentPatch.ELECTRIC_PIANO_2_CHORUSED_PIANO
				7u ->
					return MidiInstrumentPatch.HARPSICHORD
				8u ->
					return MidiInstrumentPatch.CLAVINET
				9u ->
					return MidiInstrumentPatch.CELESTA
				10u ->
					return MidiInstrumentPatch.GLOCKENSPIEL
				11u ->
					return MidiInstrumentPatch.MUSIC_BOX
				12u ->
					return MidiInstrumentPatch.VIBRAPHONE
				13u ->
					return MidiInstrumentPatch.MARIMBA
				14u ->
					return MidiInstrumentPatch.XYLOPHONE
				15u ->
					return MidiInstrumentPatch.TUBULAR_BELLS
				16u ->
					return MidiInstrumentPatch.DULCIMER_SANTUR
				17u ->
					return MidiInstrumentPatch.DRAWBAR_ORGAN_HAMMOND
				18u ->
					return MidiInstrumentPatch.PERCUSSIVE_ORGAN
				19u ->
					return MidiInstrumentPatch.ROCK_ORGAN
				20u ->
					return MidiInstrumentPatch.CHURCH_ORGAN
				21u ->
					return MidiInstrumentPatch.REED_ORGAN
				22u ->
					return MidiInstrumentPatch.ACCORDION_FRENCH
				23u ->
					return MidiInstrumentPatch.HARMONICA
				24u ->
					return MidiInstrumentPatch.TANGO_ACCORDION_BAND_NEON
				25u ->
					return MidiInstrumentPatch.ACOUSTIC_GUITAR_NYLON
				26u ->
					return MidiInstrumentPatch.ACOUSTIC_GUITAR_STEEL
				27u ->
					return MidiInstrumentPatch.ELECTRIC_GUITAR_JAZZ
				28u ->
					return MidiInstrumentPatch.ELECTRIC_GUITAR_CLEAN
				29u ->
					return MidiInstrumentPatch.ELECTRIC_GUITAR_MUTED
				30u ->
					return MidiInstrumentPatch.OVERDRIVEN_GUITAR
				31u ->
					return MidiInstrumentPatch.DISTORTION_GUITAR
				32u ->
					return MidiInstrumentPatch.GUITAR_HARMONICS
				33u ->
					return MidiInstrumentPatch.ACOUSTIC_BASS
				34u ->
					return MidiInstrumentPatch.ELECTRIC_BASS_FINGERED
				35u ->
					return MidiInstrumentPatch.ELECTRIC_BASS_PICKED
				36u ->
					return MidiInstrumentPatch.FRETLESS_BASS
				37u ->
					return MidiInstrumentPatch.SLAP_BASS_1
				38u ->
					return MidiInstrumentPatch.SLAP_BASS_2
				39u ->
					return MidiInstrumentPatch.SYNTH_BASS_1
				40u ->
					return MidiInstrumentPatch.SYNTH_BASS_2
				41u ->
					return MidiInstrumentPatch.VIOLIN
				42u ->
					return MidiInstrumentPatch.VIOLA
				43u ->
					return MidiInstrumentPatch.CELLO
				44u ->
					return MidiInstrumentPatch.CONTRABASS
				45u ->
					return MidiInstrumentPatch.TREMOLO_STRINGS
				46u ->
					return MidiInstrumentPatch.PIZZICATO_STRINGS
				47u ->
					return MidiInstrumentPatch.ORCHESTRAL_HARP
				48u ->
					return MidiInstrumentPatch.TIMPANI
				49u ->
					return MidiInstrumentPatch.STRING_ENSEMBLE_1_STRINGS
				50u ->
					return MidiInstrumentPatch.STRING_ENSEMBLE_2_SLOW_STRINGS
				51u ->
					return MidiInstrumentPatch.SYNTHSTRINGS_1
				52u ->
					return MidiInstrumentPatch.SYNTHSTRINGS_2
				53u ->
					return MidiInstrumentPatch.CHOIR_AAHS
				54u ->
					return MidiInstrumentPatch.VOICE_OOHS
				55u ->
					return MidiInstrumentPatch.SYNTH_VOICE
				56u ->
					return MidiInstrumentPatch.ORCHESTRA_HIT
				57u ->
					return MidiInstrumentPatch.TRUMPET
				58u ->
					return MidiInstrumentPatch.TROMBONE
				59u ->
					return MidiInstrumentPatch.TUBA
				60u ->
					return MidiInstrumentPatch.MUTED_TRUMPET
				61u ->
					return MidiInstrumentPatch.FRENCH_HORN
				62u ->
					return MidiInstrumentPatch.BRASS_SECTION
				63u ->
					return MidiInstrumentPatch.SYNTHBRASS_1
				64u ->
					return MidiInstrumentPatch.SYNTHBRASS_2
				65u ->
					return MidiInstrumentPatch.SOPRANO_SAX
				66u ->
					return MidiInstrumentPatch.ALTO_SAX
				67u ->
					return MidiInstrumentPatch.TENOR_SAX
				68u ->
					return MidiInstrumentPatch.BARITONE_SAX
				69u ->
					return MidiInstrumentPatch.OBOE
				70u ->
					return MidiInstrumentPatch.ENGLISH_HORN
				71u ->
					return MidiInstrumentPatch.BASSOON
				72u ->
					return MidiInstrumentPatch.CLARINET
				73u ->
					return MidiInstrumentPatch.PICCOLO
				74u ->
					return MidiInstrumentPatch.FLUTE
				75u ->
					return MidiInstrumentPatch.RECORDER
				76u ->
					return MidiInstrumentPatch.PAN_FLUTE
				77u ->
					return MidiInstrumentPatch.BLOWN_BOTTLE
				78u ->
					return MidiInstrumentPatch.SHAKUHACHI
				79u ->
					return MidiInstrumentPatch.WHISTLE
				80u ->
					return MidiInstrumentPatch.OCARINA
				81u ->
					return MidiInstrumentPatch.LEAD_1_SQUARE_WAVE
				82u ->
					return MidiInstrumentPatch.LEAD_2_SAWTOOTH_WAVE
				83u ->
					return MidiInstrumentPatch.LEAD_3_CALLIOPE
				84u ->
					return MidiInstrumentPatch.LEAD_4_CHIFFER
				85u ->
					return MidiInstrumentPatch.LEAD_5_CHARANG
				86u ->
					return MidiInstrumentPatch.LEAD_6_VOICE_SOLO
				87u ->
					return MidiInstrumentPatch.LEAD_7_FIFTHS
				88u ->
					return MidiInstrumentPatch.LEAD_8_BASS_LEAD
				89u ->
					return MidiInstrumentPatch.PAD_1_NEW_AGE_FANTASIA
				90u ->
					return MidiInstrumentPatch.PAD_2_WARM
				91u ->
					return MidiInstrumentPatch.PAD_3_POLYSYNTH
				92u ->
					return MidiInstrumentPatch.PAD_4_CHOIR_SPACE_VOICE
				93u ->
					return MidiInstrumentPatch.PAD_5_BOWED_GLASS
				94u ->
					return MidiInstrumentPatch.PAD_6_METALLIC_PRO
				95u ->
					return MidiInstrumentPatch.PAD_7_HALO
				96u ->
					return MidiInstrumentPatch.PAD_8_SWEEP
				97u ->
					return MidiInstrumentPatch.FX_1_RAIN
				98u ->
					return MidiInstrumentPatch.FX_2_SOUNDTRACK
				99u ->
					return MidiInstrumentPatch.FX_3_CRYSTAL
				100u ->
					return MidiInstrumentPatch.FX_4_ATMOSPHERE
				101u ->
					return MidiInstrumentPatch.FX_5_BRIGHTNESS
				102u ->
					return MidiInstrumentPatch.FX_6_GOBLINS
				103u ->
					return MidiInstrumentPatch.FX_7_ECHOES_DROPS
				104u ->
					return MidiInstrumentPatch.FX_8_SCI_FI_STAR_THEME
				105u ->
					return MidiInstrumentPatch.SITAR
				106u ->
					return MidiInstrumentPatch.BANJO
				107u ->
					return MidiInstrumentPatch.SHAMISEN
				108u ->
					return MidiInstrumentPatch.KOTO
				109u ->
					return MidiInstrumentPatch.KALIMBA
				110u ->
					return MidiInstrumentPatch.BAG_PIPE
				111u ->
					return MidiInstrumentPatch.FIDDLE
				112u ->
					return MidiInstrumentPatch.SHANAI
				113u ->
					return MidiInstrumentPatch.TINKLE_BELL
				114u ->
					return MidiInstrumentPatch.AGOGO
				115u ->
					return MidiInstrumentPatch.STEEL_DRUMS
				116u ->
					return MidiInstrumentPatch.WOODBLOCK
				117u ->
					return MidiInstrumentPatch.TAIKO_DRUM
				118u ->
					return MidiInstrumentPatch.MELODIC_TOM
				119u ->
					return MidiInstrumentPatch.SYNTH_DRUM
				120u ->
					return MidiInstrumentPatch.REVERSE_CYMBAL
				121u ->
					return MidiInstrumentPatch.GUITAR_FRET_NOISE
				122u ->
					return MidiInstrumentPatch.BREATH_NOISE
				123u ->
					return MidiInstrumentPatch.SEASHORE
				124u ->
					return MidiInstrumentPatch.BIRD_TWEET
				125u ->
					return MidiInstrumentPatch.TELEPHONE_RING
				126u ->
					return MidiInstrumentPatch.HELICOPTER
				127u ->
					return MidiInstrumentPatch.APPLAUSE
				128u ->
					return MidiInstrumentPatch.GUNSHOT
				else ->
					return MidiInstrumentPatch.ACOUSTIC_GRAND_PIANO
			}//End switch
		}

		fun valueToPrecussionPatch(precussionValue:UByte) : PrecussionKeyMap
		{
			when(precussionValue.toUInt()) {
				35u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_ONE_B, MidiPrecussionMap.ACOUSTIC_BASS_DRUM)
				36u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_C, MidiPrecussionMap.BASS_DRUM_1)
				37u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_C_SHRP, MidiPrecussionMap.SIDE_STICK)
				38u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_D, MidiPrecussionMap.ACOUSTIC_SNARE)
				39u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_D_SHRP, MidiPrecussionMap.HAND_CLAP)
				40u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_E, MidiPrecussionMap.ELECTRIC_SNARE)
				41u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_F, MidiPrecussionMap.LOW_FLOOR_TOM)
				42u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_F_SHRP, MidiPrecussionMap.CLOSED_HI_HAT)
				43u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_G, MidiPrecussionMap.HIGH_FLOOR_TOM)
				44u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_G_SHRP, MidiPrecussionMap.PEDAL_HI_HAT)
				45u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_A, MidiPrecussionMap.LOW_TOM)
				46u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_A_SHARP, MidiPrecussionMap.OPEN_HI_HAT)
				47u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_TWO_B, MidiPrecussionMap.LOW_MID_TOM)
				48u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_C, MidiPrecussionMap.HI_MID_TOM)
				49u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_C_SHRP, MidiPrecussionMap.CRASH_CYMBAL_1)
				50u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_D, MidiPrecussionMap.HIGH_TOM)
				51u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_D_SHRP, MidiPrecussionMap.RIDE_CYMBAL_1)
				52u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_E, MidiPrecussionMap.CHINESE_CYMBAL)
				53u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_F, MidiPrecussionMap.RIDE_BELL)
				54u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_F_SHRP, MidiPrecussionMap.TAMBOURINE)
				55u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_G, MidiPrecussionMap.SPLASH_CYMBAL)
				56u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_G_SHRP, MidiPrecussionMap.COWBELL)
				57u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_A, MidiPrecussionMap.CRASH_CYMBAL_2)
				58u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_A_SHARP, MidiPrecussionMap.VIBRASLAP)
				59u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_THREE_B, MidiPrecussionMap.RIDE_CYMBAL_2)
				60u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_C, MidiPrecussionMap.HI_BONGO)
				61u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_C_SHRP, MidiPrecussionMap.LOW_BONGO)
				62u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_D, MidiPrecussionMap.MUTE_HI_CONGA)
				63u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_D_SHRP, MidiPrecussionMap.OPEN_HI_CONGA)
				64u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_E, MidiPrecussionMap.LOW_CONGA)
				65u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_F, MidiPrecussionMap.HIGH_TIMBALE)
				66u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_F_SHRP, MidiPrecussionMap.LOW_TIMBALE)
				67u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_G, MidiPrecussionMap.HIGH_AGOGO)
				68u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_G_SHRP, MidiPrecussionMap.LOW_AGOGO)
				69u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_A, MidiPrecussionMap.CABASA)
				70u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_A_SHARP, MidiPrecussionMap.MARACAS)
				71u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FOUR_B, MidiPrecussionMap.SHORT_WHISTLE)
				72u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_C, MidiPrecussionMap.LONG_WHISTLE)
				73u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_C_SHRP, MidiPrecussionMap.SHORT_GUIRO)
				74u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_D, MidiPrecussionMap.LONG_GUIRO)
				75u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_D_SHRP, MidiPrecussionMap.CLAVES)
				76u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_E, MidiPrecussionMap.HI_WOOD_BLOCK)
				77u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_F, MidiPrecussionMap.LOW_WOOD_BLOCK)
				78u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_F_SHRP, MidiPrecussionMap.MUTE_CUICA)
				79u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_G, MidiPrecussionMap.OPEN_CUICA)
				80u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_G_SHRP, MidiPrecussionMap.MUTE_TRIANGLE)
				81u ->
					return PrecussionKeyMap(precussionValue, MidiNote.OCTAVE_FIVE_A, MidiPrecussionMap.OPEN_TRIANGLE)
				else ->
					return PrecussionKeyMap(35u, MidiNote.OCTAVE_ONE_B, MidiPrecussionMap.ACOUSTIC_BASS_DRUM)
			}//End Switch
		}

		fun valuesToMusicalKey(numShrpFlats:Byte, MajMin:UByte) : MusicalKey
		{
			var musicalKey:MusicalKey = MusicalKey.C_MAJ

			if(numShrpFlats.toInt() == 0)
			{
				return if(MajMin.toUInt() == 0u) MusicalKey.C_MAJ else MusicalKey.A_MIN
			}

			//Is 4th since the circle of 5ths counter-clockwise is the circle of
			//4ths and these are the keys which contain flats instead of sharps.
			var is4th:Boolean = (numShrpFlats < 0)

			when(abs(numShrpFlats.toInt())) {
 				1 ->
					if(is4th == false) {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.G_MAJ else MusicalKey.E_MIN}
					else {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.F_MAJ else MusicalKey.D_MIN}
				2 ->
					if(is4th == false) {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.D_MAJ else MusicalKey.B_MIN}
					else {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.BFLAT_MAJ else MusicalKey.G_MIN}
				3 ->
					if(is4th == false) {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.A_MAJ else MusicalKey.FSHRP_MIN}
					else {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.EFLAT_MAJ else MusicalKey.C_MIN}
				4 ->
					if(is4th == false) {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.E_MAJ else MusicalKey.CSHRP_MIN}
					else {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.AFLAT_MAJ else MusicalKey.F_MIN}
				5 ->
					if(is4th == false) {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.B_MAJ else MusicalKey.GSHRP_MIN}
					else {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.DFLAT_MAJ else MusicalKey.BFLAT_MIN}
				6 ->
					if(is4th == false) {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.FSHRP_MAJ else MusicalKey.DSHRP_MIN}
					else {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.GFLAT_MAJ else MusicalKey.EFLAT_MIN}
				7 ->
					if(is4th == false) {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.CSHRP_MAJ else MusicalKey.ASHRP_MIN}
					else {musicalKey = if(MajMin.toUInt() == 0u) MusicalKey.CFLAT_MAJ else MusicalKey.AFLAT_MIN}
				else ->
					{}
			}

			return musicalKey
		}
	}
}