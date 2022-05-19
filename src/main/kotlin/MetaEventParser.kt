import java.util.concurrent.atomic.AtomicInteger

class MetaEventParser
{
	private var m_Data:ByteArray? = null
	private var m_TimeDelta:UInt = 0u

	fun parseMetaEvent(startIdx:AtomicInteger, timeDelta:UInt, data:ByteArray) : IEvent?
	{
		m_Data = data
		m_TimeDelta = timeDelta

		var tmpIdx:Int = startIdx.get()
		val metaSimpleID:UShort = Utils.into16Bit(m_Data!![tmpIdx].toUByte(), m_Data!![tmpIdx + 1].toUByte())
		startIdx.addAndGet(2)

		var metaEventType:MetaEventDefinitions = MetaEventDefinitions.UNKNOWN

		for (evtID in enumValues<MetaEventDefinitions>())
		{
			if(evtID.num == metaSimpleID){
				metaEventType = evtID
				break
			}
		}

		if(metaEventType == MetaEventDefinitions.UNKNOWN)  {
			m_Data = null //Just for cleanliness. It's reset each time the function is called, but still good practice.
			return null
		}

		var event:IEvent? = null//parseAppropriateMetaEvent(startIdx:&startIdx, eventType:metaEventType)

		m_Data = null

		return event
	}

	private fun parseAppropriateMetaEvent(startIdx:AtomicInteger, eventType:MetaEventDefinitions) : IEvent?
	{
		var event:IEvent? = null

		when(eventType) {
			MetaEventDefinitions.SEQUENCE_NUMBER ->
				event = parseSeqNumber(startIdx)
			MetaEventDefinitions.TEXT_INFO ->
				event = parseTextEvent(startIdx)
			MetaEventDefinitions.COPYRIGHT ->
				event = parseCopyright(startIdx)
			MetaEventDefinitions.TEXT_SEQUENCE ->
				event = parseTextSequence(startIdx)
			MetaEventDefinitions.TEXT_INSTRUMENT ->
				event = parseTextInstrument(startIdx)
			MetaEventDefinitions.TEXT_LYRIC ->
				event = parseTextLyric(startIdx)
			MetaEventDefinitions.TEXT_MARKER ->
				event = parseTextMarker(startIdx)
			MetaEventDefinitions.TEXT_CUE_POINT ->
				event = parseTextCuePoint(startIdx)
			MetaEventDefinitions.MIDI_CHANNEL ->
				event = parseMidiChannel(startIdx)
			MetaEventDefinitions.PORT_SELECTION ->
				event = parsePortSelection(startIdx)
			MetaEventDefinitions.TEMPO ->
				event = parseTempo(startIdx)
			MetaEventDefinitions.SMPTE ->
				event = parseSmpte(startIdx)
			MetaEventDefinitions.TIME_SIGNATURE ->
				event = parseTimeSignature(startIdx)
			MetaEventDefinitions.MINI_TIME_SIGNATURE ->
				event = parseMiniTimeSignature(startIdx)
			MetaEventDefinitions.SPECIAL_SEQUENCE ->
				event = parseSpecialSequence(startIdx)
			MetaEventDefinitions.END_OF_TRACK ->//0xFF2F
				{} //Already handled
			else ->
				{}
		}

		return event
	}

	//0xFF00 - Will be followed by 02 then the sequence number
	private fun parseSeqNumber(startIdx:AtomicInteger) : IEvent?
	{
		//return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.SEQUENCE_NUMBER)
		return null
	}

	//All 0xFF2[0 - F]

	//0xFF20 -
	private fun parseMidiChannel(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the Channel Prefix metaevent is 0xFF2001
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x01u) return null //Casting because Kotlin has serious issues handling unsigend variables across the board


		val midiChannel:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		Printer.printUInt8AsHex(midiChannel)

//		return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.MIDI_CHANNEL)
		return null
	}

	//0xFF21 - //Also has a 01 after the 21, and then a byte (0 - 127) Identifing the port number.
	private fun parsePortSelection(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the port selection metaevent is 0xFF2101
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x01u) return null

		val portNumber:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		Printer.printUInt8AsHex(portNumber)

//		return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.PORT_SELECTION)
		return null
	}

	//All 0xFF5[0 - F]
	//0xFF51 -
	private fun parseTempo(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the tempo metaevent is 0xFF5103
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x03u) return null

		//This will ultimately be used to store the 24 bit tempo
		var tempoValue:UInt

		var MSB:UInt = m_Data!![startIdx.get()].toUInt()   //UInt32(m_Data[startIdx])
		MSB = MSB shl 16  //(MSB << 16)
		startIdx.incrementAndGet()

		var MID_VAL:UInt =  m_Data!![startIdx.get()].toUInt()  //UInt32(m_Data[startIdx])
		MID_VAL = MID_VAL shl 8  //(MID_VAL << 8)
		startIdx.incrementAndGet()

		tempoValue = MSB + MID_VAL + m_Data!![startIdx.get()].toUInt()    //UInt32(m_Data[startIdx])
		startIdx.incrementAndGet()

		Printer.printUInt32AsHex(tempoValue)

//		return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.TEMPO)
		return null
	}

	//0xFF54 -
	private fun parseSmpte(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the SMPTE metaevent is 0xFF5405
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x05u) return null

		//hr
		val hours:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//mn
		val minutes:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//se
		val seconds:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//fr
		val milliseconds:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//ff
		val fractionalFrames:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		Printer.printByteValuesAsHex(hours, minutes, seconds, milliseconds)
		Printer.printByteValuesAsHex(fractionalFrames)

//		return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.SMPTE)
		return null
	}

	//0xFF58 -
	private fun parseTimeSignature(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the time signature metaevent is 0xFF5804
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x04u) return null

		val numerator:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//The denominator is a negative power of two. ie: 1^(-3) is really 1/3
		val denominator:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		var timing:TimingInfo = Utils.timeSignatureFromNumeratorDenominator(numerator, denominator)

		val midiClocksInMetronomeClick:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		val numberNotated32ndNotes:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		Printer.printByteValuesAsHex(numerator)
		Printer.printByteValuesAsHex(denominator)
		Printer.printByteValuesAsHex(midiClocksInMetronomeClick)
		Printer.printByteValuesAsHex(numberNotated32ndNotes)

//		return MetaEvent(eventTimeDelta: m_TimeDelta,
//			trackTiming: timing,
//			numberNotated32ndNotes: numberNotated32ndNotes,
//			midiClocksInMetronomeClick: midiClocksInMetronomeClick,
//			metaeventType:.TIME_SIGNATURE)
		return null
	}

	//0xFF59 -
	private fun parseMiniTimeSignature(startIdx:AtomicInteger) : IEvent?
	{
		val constByte:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		//Via the spec - the full definition of the mini-time signature metaevent is 0xFF5902
		//Our enum is only UInt16 since not all meta-events use 3 bytes, therefore, we need
		//to perform an extra validation here.
		if(constByte.toUInt() != 0x02u) return null

		//We need this to be signed!
		//Negative represents the number of flats [-1 through -7]
		//Positive represents the number of sharps [1 through 7]
		//Zero reprsents C Major/A Minor
		val numberSharpsFlats:Byte = m_Data!![startIdx.get()]  // Int8(m_Data[startIdx]) //We WANT the sign bit
		startIdx.incrementAndGet()

		//0 = Major key
		//1 = Minor key
		val MajorMinorKey:UByte = m_Data!![startIdx.get()].toUByte()
		startIdx.incrementAndGet()

		val trackKey:MusicalKey = Utils.valuesToMusicalKey(numberSharpsFlats, MajorMinorKey)
		//Printer.printMessage(MusicalKey.musicalKeyToString(trackKey))

//		return MetaEvent(eventTimeDelta: m_TimeDelta, trackKey:trackKey, metaeventType:.MINI_TIME_SIGNATURE)
		return null
	}

	//Events with 0xFF7[0 - F]

	//0xFF7F -
	private fun parseSpecialSequence(startIdx:AtomicInteger) : IEvent?
	{
		//return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.SPECIAL_SEQUENCE)
		return null
	}

	//All meta-text events are 0xFF0[1 - F]
	//NOTE: In the text events, there is a size of the text + 1. It seems this is
	//a value of 0x00 for the string's null terminator. ie: '\0'
	//0xFF01, //Followed by LEN, TEXT. NOTE: The 0xFF01 - 0xFF0F are all reserved for text messages.
	private fun parseTextEvent(startIdx:AtomicInteger) : IEvent?
	{
		//return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.TEXT_INFO)
		return null
	}

	//0xFF02 -
	private fun parseCopyright(startIdx:AtomicInteger) : IEvent?
	{
		//return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.COPYRIGHT)
		return null
	}

	//0xFF03 -
	private fun parseTextSequence(startIdx:AtomicInteger) : IEvent?
	{
		val textLen:UInt = Utils.readVariableLengthValue(startIdx, m_Data!!) ?: 0u

		var textInfo:String
		val textData:ArrayList<UByte> = ArrayList<UByte>()

		//We have characters which were read in as unsigned bytes, so
		//we need to isolate the character bytes to get into a string.
		for (idx in 0..textLen.toInt()) {
			textData.add(m_Data!![idx + startIdx.get()].toUByte())
		}

		textInfo = textData.toString()

		startIdx.addAndGet(textLen.toInt())
		Printer.printMessage(textInfo)

//		return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.TEXT_SEQUENCE)
		return null
	}

	//0xFF04 -
	private fun parseTextInstrument(startIdx:AtomicInteger) : IEvent?
	{
		//return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.TEXT_INSTRUMENT)
		return null
	}

	//0xFF05 -
	private fun parseTextLyric(startIdx:AtomicInteger) : IEvent?
	{
		//return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.TEXT_LYRIC)
		return null
	}

	//0xFF06 -
	private fun parseTextMarker(startIdx:AtomicInteger) : IEvent?
	{
		//return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.TEXT_MARKER)
		return null
	}

	//0xFF07 -
	private fun parseTextCuePoint(startIdx:AtomicInteger) : IEvent?
	{
		//return MetaEvent(eventTimeDelta: m_TimeDelta, metaeventType:.TEXT_CUE_POINT)
		return null
	}
}