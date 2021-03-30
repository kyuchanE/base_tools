package com.chan9u.basetools.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.chan9u.basetools.base.BaseActivity
import com.chan9u.basetools.custom.glide.GlideApp
import com.google.gson.*
import com.jaychang.st.SimpleText
import com.orhanobut.hawk.Hawk
import es.dmoral.toasty.Toasty
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import pyxis.uzuki.live.richutilskt.utils.RConstructorBuilder
import pyxis.uzuki.live.richutilskt.utils.browse


/*------------------------------------------------------------------------------
 * DESC    : 확장함수
 *------------------------------------------------------------------------------*/

////////////////////////////// Any //////////////////////////////

fun Any?.log(prefix: String = ""): Any? {
    when (this) {
        is Boolean, is Int, is Long, is Float, is Double -> L.d(prefix + toString())
        is Throwable -> L.e(this)
        is String -> L.d(prefix + this)
        else -> L.d(prefix + this)
    }
    return this
}

fun Any?.save(key: String) {
    if (this.isNull) {
        Hawk.delete(key)
        "Hawk delete : $key".log()
    } else {
        Hawk.put(key, this)
        "Hawk save : $key = $this".log()
    }
}

val Any?.notNull get() = this != null
val Any?.isNull get() = this == null
val Any?.unit get() = null
val Any.asJsonObject: JsonObject
    get() = try {
    JsonParser.parseString(Gson().toJson(this)).asJsonObject
} catch (e: Exception) {
    "val Any.asJsonObject 예외".log()
    JsonObject()
}

////////////////////////////// List //////////////////////////////

val List<*>?.lastIndex get() = if (isNullOrEmpty()) 0 else this!!.size - 1
fun List<*>?.isLast(index: Int) = !isNullOrEmpty() && lastIndex == index

////////////////////////////// DataBinding //////////////////////////////

val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)

val View.layoutInflater get() = context.layoutInflater
fun <T : ViewDataBinding> View.bind() = DataBindingUtil.bind<T>(this) as T
fun <T : ViewDataBinding> LayoutInflater.bind(layoutId: Int, parent: ViewGroup? = null, attachToParent: Boolean = false): T {
    return DataBindingUtil.inflate(this, layoutId, parent, attachToParent)
}

fun <T : ViewDataBinding> ViewGroup.bind(layoutId: Int, attachToParent: Boolean = false): T {
    return DataBindingUtil.inflate(layoutInflater, layoutId, this, attachToParent)
}

fun <T : ViewDataBinding> Activity.bind(layoutId: Int): T {
    return DataBindingUtil.setContentView(this, layoutId)
}

fun <T : ViewDataBinding> Activity.bindView(layoutId: Int, parent: ViewGroup? = null, attachToRoot: Boolean = false): T {
    return DataBindingUtil.inflate(layoutInflater, layoutId, parent, attachToRoot)
}

fun ViewDataBinding.setOnEvents(activity: BaseActivity<*>? = null) = root.setOnEvents(activity)

//fun ViewDataBinding.setOnMenuEvents(baseActivity: BaseActivity<*>? = null): ViewDataBinding {
//    (baseActivity ?: root.activity)?.let { h ->
//        (root as? ViewGroup)?.eventViews?.filter { it.id != View.NO_ID }?.forEach {
//            when (it) {
//                is CompoundButton -> it.setOnClickListener(h::onRxMenuEvents)
//                is Button, is ImageButton, is CheckedTextView -> it.setOnClickListener(h::onRxMenuEvents)
//            }
//
//            if (it.isClick) it.setOnClickListener(h::onRxMenuEvents)
//        }
//    }
//    return this
//}


////////////////////////////// View //////////////////////////////

val View.isClick get() = tag == "click"

val View.activity: BaseActivity<*>?
    get() {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is BaseActivity<*>) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

val ViewGroup.views: List<View>
    get() {
        val views = mutableListOf<View>()
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is ViewGroup) {
                views.addAll(child.views)
            }

            views.add(child)
        }
        return views
    }

val ViewGroup.eventViews: List<View>
    get() {
        val result = mutableListOf<View>()

        for (view in views) {
            when (view) {
                is Button,
                is ImageButton,
                is CompoundButton,
                is CheckedTextView,
                is RadioButton,
                is CheckBox
                -> result.add(view)
            }
            if (view.isClick) result.add(view)
        }
        return result
    }

fun View.setOnEvents(baseActivity: BaseActivity<*>? = null): View {
    var views = mutableListOf<View>()

    if (this is ViewGroup) views.addAll(eventViews)
    else views.add(this)

    val handler = baseActivity ?: activity
    handler?.let { h ->
        views.filter { it.id != View.NO_ID }.forEach {
            when (it) {
                is CompoundButton -> {
                    it.setOnCheckedChangeListener(h::onRxCheckedEvents)
                    it.setOnClickListener(h::onRxBtnEvents)
                }
                is Button, is ImageButton, is CheckedTextView -> it.setOnClickListener(h::onRxBtnEvents)
            }

            if (it.isClick) it.setOnClickListener(h::onRxBtnEvents)
        }
    }

    return this
}

fun View.show(): View {
    visibility = View.VISIBLE
    return this
}

fun View.hide(): View {
    visibility = View.INVISIBLE
    return this
}

fun View.gone(): View {
    visibility = View.GONE
    return this
}

fun View.visible(isShow: Boolean): View {
    if (isShow) {
        show()
    } else {
        gone()
    }
    return this
}

inline fun View.showIf(cond: () -> Boolean): View {
    if (visibility != View.VISIBLE && cond()) {
        visibility = View.VISIBLE
    }
    return this
}

inline fun View.hideIf(cond: () -> Boolean): View {
    if (visibility != View.INVISIBLE && cond()) {
        visibility = View.INVISIBLE
    }
    return this
}

inline fun View.goneIf(cond: () -> Boolean): View {
    if (visibility != View.GONE && cond()) {
        visibility = View.GONE
    }
    return this
}

fun View.afterMeasured(action: View.() -> Unit): View {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                action()
            }
        }
    })
    return this
}

val View.parentView: ViewGroup get() = parent as ViewGroup

fun <T : View> View.find(@IdRes id: Int) : T = findViewById(id)


////////////////////////////// TextView //////////////////////////////

val TextView.value get() = text.toString()
val TextView.simple get() = value.simple


////////////////////////////// String //////////////////////////////

val String.md5: String
    get() {
        val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }

val String.sha1: String
    get() {
        val bytes = MessageDigest.getInstance("SHA-1").digest(this.toByteArray())
        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }

val String.sha256: String
    get() {
        val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }

val String?.asColor: Int
    get() = try {
        Color.parseColor(this)
    } catch (e: IllegalArgumentException) {
        0
    }

val String.boolean: Boolean get() = "Y".equals(this, true)

val String.cardNo: String
    get() {
        val preparedString = replace(" ", "").trim()
        val result = StringBuilder()
        for (i in preparedString.indices) {
            if (i % 4 == 0 && i != 0) {
                result.append(" ")
            }
            result.append(preparedString[i])
        }
        return result.toString()
    }

val String.extInt: Int
    get() {
        if (isNullOrEmpty()) return 0
        val count = replace("[^0-9\\-]".toRegex(), "")
        return if (count.isEmpty() || count == "-") 0 else count.toInt(10)
    }

val String.extLong: Long
    get() {
        if (isNullOrEmpty()) return 0
        val count = replace("[^0-9\\-]".toRegex(), "")
        return if (count.isEmpty() || count == "-") 0 else count.toLong(10)
    }

val String.count: String get() = extInt.count
val String.simple: SimpleText get() = SimpleText.from(this)
val String.date: String get() = SimpleDateFormat(this, Locale.KOREA).format(Date())
fun String.date(date: Date): String = SimpleDateFormat(this, Locale.KOREA).format(date)
fun String.date(fromFormat: String, dateStr: String): String  {
    val fromDateFormat  = SimpleDateFormat(fromFormat, Locale.KOREA)
    val toDateFormat    = SimpleDateFormat(this, Locale.KOREA)
    return toDateFormat.format(fromDateFormat.parse(dateStr)!!)
}
val String.visible: Boolean get() = !isNullOrEmpty()
val String.asJsonObject: JsonObject get() = try {
    JsonParser.parseString(this).asJsonObject
} catch (e: Exception) {
    "val String.asJsonObject 예외".log()
    JsonObject()
}

////////////////////////////// Int //////////////////////////////

val Int.digit get() = if (this < 10) "0${toString()}" else toString()
val Int.px2dp get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.dp2px get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.boolean get() = this > 0
val Int.count get() = String.format(Locale.KOREA, "%,d", this)

////////////////////////////// Long //////////////////////////////

val Long.count get() = String.format(Locale.KOREA, "%,d", this)

////////////////////////////// Float //////////////////////////////

val Float.px2dp get() = (this / Resources.getSystem().displayMetrics.density)
val Float.dp2px get() = (this * Resources.getSystem().displayMetrics.density)

////////////////////////////// Double //////////////////////////////

val Double.count get() = String.format(Locale.KOREA, "%,.1f", this)

////////////////////////////// Boolean //////////////////////////////

val Boolean.visible get() = if (this) View.VISIBLE else View.GONE
val Boolean.bit get() = if (this) 1 else 0
val Boolean.yn get() = if (this) "Y" else "N"

////////////////////////////// Hawk //////////////////////////////

fun <T> hawk(key: String): T = Hawk.get(key)
fun <T> hawk(key: String, default: T): T = Hawk.get(key, default)
fun <T> flash(key: String): T = Hawk.get<T>(key).also { Hawk.delete(key) }
fun <T> flash(key: String, default: T): T = Hawk.get(key, default).also { Hawk.delete(key) }

////////////////////////////// RecyclerView //////////////////////////////

class SectionSetter {
    var sectionId: Int = 0
    var params: (SectionParameters.Builder.() -> SectionParameters.Builder) = { this }
}

inline fun <reified T : Section> section(action: SectionSetter.() -> Unit): T = SectionSetter().run {
    action()

    RConstructorBuilder(T::class.java)
        .addParameter(
            Int::class.java,
            sectionId
        )
        .addParameter(
            SectionParameters::class.java,
            SectionParameters.builder().params().build()
        )
        .newInstance()
}

fun <T : Section> RecyclerView.addSection(action: () -> T): T {
    val section = action()

    adapter?.let {
        (it as SectionedRecyclerViewAdapter).addSection(section)
        adapter!!.notifyDataSetChanged()
    } ?: run {
        val sectionedAdapter = SectionedRecyclerViewAdapter()
        sectionedAdapter.addSection(section)
        adapter = sectionedAdapter
    }

    return section
}

////////////////////////////// Context //////////////////////////////

fun Context.getInteger(@IntegerRes id: Int) = resources.getInteger(id)

fun Context.getBoolean(@BoolRes id: Int) = resources.getBoolean(id)
fun Context.getColor(@ColorRes id: Int) = ContextCompat.getColor(this, id)
fun Context.getDrawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

fun Context.toast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT, gravity: Int = Gravity.BOTTOM) {
    val toast = Toasty.normal(this, resource, duration)
    toast.setGravity(gravity, 0, if (gravity == Gravity.BOTTOM) 30.dp2px else 0)
    toast.show()
}

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT, gravity: Int = Gravity.BOTTOM) {
    val toast = Toasty.normal(this, text, duration)
    toast.setGravity(gravity, 0, if (gravity == Gravity.BOTTOM) 30.dp2px else 0)
    toast.show()
}

fun Context.market(name: String? = null) {
    if (!browse("market://details?id=${name ?: packageName}", true)) {
        browse("http://play.google.com/store/apps/details?id=${name ?: packageName}", true)
    }
}

////////////////////////////// JsonObject //////////////////////////////

fun JsonElement?.path(pathStr: String): JsonElement? {
    val pathList = pathStr.split("/")
    var element = this
    for (path in pathList) {
        element = try {
            if (path.isDigitsOnly()) {
                if (element is JsonArray) {
                    element[path.toInt()]
                } else {
                    element?.asJsonArray?.get(path.toInt())
                }
            } else {
                element?.asJsonObject?.get(path)
            }
        } catch (e: Exception) {
            null
        }
    }

    return if (element is JsonNull) null else element
}

fun JsonElement?.find(key: String): List<JsonElement> {
    val values = mutableListOf<JsonElement>()
    this?.let {
        when {
            isJsonArray -> {
                asJsonArray.forEach {
                    values.addAll(it.find(key))
                }
            }
            isJsonObject -> {
                asJsonObject.keySet().forEach {
                    if (key == it) {
                        values.add(asJsonObject[it])
                    } else {
                        values.addAll(asJsonObject[it].find(key))
                    }
                }
            }
        }
    }
    return values
}

fun JsonElement?.findFirst(key: String): JsonElement? {
    val values = find(key)
    return if (!values.isNullOrEmpty() && values[0] !is JsonNull) values[0] else null
}
fun JsonElement?.findLast(key: String): JsonElement? {
    val values = find(key)
    return if (!values.isNullOrEmpty() && values.last() !is JsonNull) values.last() else null
}

inline fun <reified T> JsonElement?.value(pathStr: String): T {
    val result: Any = when (T::class) {
        String::class, CharSequence::class -> path(pathStr)?.asString ?: ""
        Boolean::class -> path(pathStr)?.asBoolean ?: false
        Int::class -> path(pathStr)?.asInt ?: 0
        Long::class -> path(pathStr)?.asLong ?: 0L
        Float::class -> path(pathStr)?.asFloat ?: 0F
        Double::class -> path(pathStr)?.asDouble ?: 0.0
        JsonObject::class -> path(pathStr)?.asJsonObject ?: JsonObject()
        JsonArray::class -> path(pathStr)?.asJsonArray ?: JsonArray()
        MutableList::class -> path(pathStr)?.asJsonArray?.asList ?: mutableListOf<JsonObject>()
        List::class -> path(pathStr)?.asJsonArray?.asList ?: listOf<JsonObject>()
        else -> throw TypeCastException("지원하지 않는 타입니다.")
    }

    return result as T
}

inline fun <reified T> JsonElement?.value(pathStr: String, default: T): T {
    val result = when (T::class) {
        String::class, CharSequence::class -> path(pathStr)?.asString ?: default
        Boolean::class -> path(pathStr)?.asBoolean ?: false
        Int::class -> path(pathStr)?.asInt ?: default
        Long::class -> path(pathStr)?.asLong ?: default
        Float::class -> path(pathStr)?.asFloat ?: default
        Double::class -> path(pathStr)?.asDouble ?: default
        JsonObject::class -> path(pathStr)?.asJsonObject ?: default
        JsonArray::class -> path(pathStr)?.asJsonArray ?: default
        MutableList::class -> path(pathStr)?.asJsonArray?.asList ?: default
        List::class -> path(pathStr)?.asJsonArray?.asList ?: default
        else -> throw TypeCastException("지원하지 않는 타입니다.")
    }

    return result as T
}

infix fun JsonObject.base(path: String): JsonObject = path(path)?.asJsonObject ?: JsonObject()

fun JsonObject?.asString(key: String, default: String = ""): String = try {
    this?.get(key)?.asString ?: default
} catch (e: Exception) {
    "fun JsonObject?.asString 예외".log()
    default
}

fun JsonObject?.asInt(key: String, default: Int = 0): Int = try {
    this?.get(key)?.asInt ?: default
} catch (e: Exception) {
    "fun JsonObject?.asInt 예외".log()
    default
}

fun JsonObject?.asFloat(key: String, default: Float = 0F): Float = try {
    this?.get(key)?.asFloat ?: default
} catch (e: Exception) {
    "fun JsonObject?.asFloat 예외".log()
    default
}

fun JsonObject?.asDouble(key: String, default: Double = 0.0): Double = try {
    this?.get(key)?.asDouble ?: default
} catch (e: Exception) {
    "fun JsonObject?.asDouble 예외".log()
    default
}

fun JsonObject?.asLong(key: String, default: Long = 0L): Long = try {
    this?.get(key)?.asLong ?: default
} catch (e: Exception) {
    "fun JsonObject?.asLong 예외".log()
    default
}

fun JsonObject?.asBoolean(key: String, default: Boolean = false): Boolean = try {
    this?.get(key)?.asBoolean ?: default
} catch (e: Exception) {
    "fun JsonObject?.asBoolean 예외".log()
    default
}

fun JsonObject?.asJsonObject(key: String, default: JsonObject = JsonObject()): JsonObject = try {
    this?.get(key)?.asJsonObject ?: default
} catch (e: Exception) {
    "fun JsonObject?.asJsonObject 예외".log()
    default
}

fun JsonObject?.asJsonArray(key: String, default: JsonArray = JsonArray()): JsonArray = try {
    this?.get(key)?.asJsonArray ?: default
} catch (e: Exception) {
    "fun JsonObject?.asJsonArray 예외".log()
    default
}

val JsonArray.asList: MutableList<JsonObject> get() = map { it.asJsonObject }.toMutableList()

fun JsonArray.isEmpty(): Boolean = size() == 0

////////////////////////////// ImageView //////////////////////////////

fun ImageView.load(url: String): ImageView {
    if (url.isNotEmpty()) {
        GlideApp.with(context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
    return this
}

fun ImageView.loadRound(url: String, round: Int): ImageView {
    if (url.isNotEmpty()) {
        GlideApp.with(context)
            .load(url)
            .transform(CenterCrop(), RoundedCorners(round.dp2px))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
    return this
}

fun ImageView.loadRoundTop(url: String, round: Int): ImageView {
    if (url.isNotEmpty()) {
        GlideApp.with(context)
            .load(url)
            .transform(CenterCrop(), RoundedCornersTransformation(round.dp2px, 0, RoundedCornersTransformation.CornerType.TOP))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
    return this
}

fun ImageView.loadCircle(url: String): ImageView {
    if (url.isNotEmpty()) {
        GlideApp.with(context)
            .load(url)
            .apply(RequestOptions().circleCrop())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
    return this
}