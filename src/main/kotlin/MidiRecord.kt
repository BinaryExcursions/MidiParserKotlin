class MidiRecord
{
	private var m_midiHeader:MidiRecordHeader? = null
	private lateinit var m_Tracks:ArrayList<MidiTrack>

	var header:MidiRecordHeader?
		get() = m_midiHeader
		set(value) {
			m_midiHeader = value
		}

	operator fun get(idx:Int) = m_Tracks[idx]

	fun appendTrack(track:MidiTrack) : Boolean
	{
		m_Tracks.add(track)
		return true
	}
}