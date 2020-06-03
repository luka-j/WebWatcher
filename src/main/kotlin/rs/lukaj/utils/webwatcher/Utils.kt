package rs.lukaj.utils.webwatcher

import java.util.*
import java.util.concurrent.ThreadLocalRandom


const val MS_IN_MIN = 60000

fun levenshtein(s: String, t: String): Int {
    // degenerate cases
    if (s == t)  return 0
    if (s == "") return t.length
    if (t == "") return s.length

    // create two integer arrays of distances and initialize the first one
    val v0 = IntArray(t.length + 1) { it }  // previous
    val v1 = IntArray(t.length + 1)         // current

    var cost: Int
    for (i in 0 until s.length) {
        // calculate v1 from v0
        v1[0] = i + 1
        for (j in 0 until t.length) {
            cost = if (s[i] == t[j]) 0 else 1
            v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost))
        }
        // copy v1 to v0 for next iteration
        for (j in 0 .. t.length) v0[j] = v1[j]
    }
    return v1[t.length]
}

fun String.distance(other: String) : Int {
    return levenshtein(this, other)
}

fun randomString(size: Int) : String {
    val random: Random = ThreadLocalRandom.current()
    val r = ByteArray(size)
    random.nextBytes(r)
    return Base64.getEncoder().encodeToString(r) //making it url-safe(r):
            .replace('+', '-').replace('/', '.').replace('=', '_')
}


fun binarySearchTime(input: ArrayList<Pair<Long, String>>, timeToSearch: Long) : Pair<Long, String> {
    var low = 0
    var high = input.size-1
    var mid = 0
    while(low <= high) {
        mid = (low + high)/2
        when {
            timeToSearch >input[mid].first   -> low = mid+1
            timeToSearch == input[mid].first -> return input[mid]
            timeToSearch < input[mid].first  -> high = mid-1
        }
    }
    return input[mid]
}


fun getProperty(name: String, default: String) : String {
    val javaProp = System.getProperty(name)
    if(javaProp != null) return javaProp
    val env = System.getenv(name.toUpperCase().replace('.', '_'))
    if(env != null) return env
    return default
}