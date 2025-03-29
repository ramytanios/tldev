package tldev.core

import scala.collection.immutable.HashMap

class IndexedHashMap[K, A, V] private (
    private val key: V => K,
    private val attr: V => A,
    private val kv: HashMap[K, V],
    private val ak: HashMap[A, K]
):

  require(kv.size == ak.size, "size mismatch!")

  def size: Int = if kv.size == ak.size then kv.size
  else throw new IllegalStateException("size mismatch. This is most likely a bug!")

  def keys: Set[K] = kv.keySet

  def attrs: Set[A] = ak.keySet

  def values: List[V] = kv.values.toList

  private def build(kvNew: HashMap[K, V], akNew: HashMap[A, K]) =
    new IndexedHashMap[K, A, V](this.key, this.attr, kvNew, akNew)

  def apply(attr: A): V = this.get(attr).get

  def get(attr: A): Option[V] = this.ak.get(attr).flatMap(this.kv.get)

  def remove(attr: A): IndexedHashMap[K, A, V] =
    ak.get(attr).fold(this): key =>
      val akNew = ak - attr
      val kvNew = kv - key
      this.build(kvNew, akNew)

  def -(attr: A): IndexedHashMap[K, A, V] = this.remove(attr)

  def --(attrs: List[A]): IndexedHashMap[K, A, V] =
    attrs.foldLeft(this)((m, attr) => m - attr)

  def add(v: V): IndexedHashMap[K, A, V] =
    val key  = this.key(v)
    val attr = this.attr(v)

    // check for key change
    def keyChange(t: IndexedHashMap[K, A, V]) =
      t.ak.get(attr).fold(this.build(t.kv + (key -> v), t.ak + (attr -> key))): kOld =>
        this.build((t.kv - kOld) + (key -> v), t.ak + (attr -> key))

    // check for attribute change
    def attrChange(t: IndexedHashMap[K, A, V]) =
      t.kv.get(key).fold(keyChange(t)): vOld =>
        val attrOld = this.attr(vOld)
        this.build(t.kv + (key -> v), (t.ak - attrOld) + (attr -> key))

    attrChange(this)

  def +(v: V): IndexedHashMap[K, A, V] = this.add(v)

  def ++(vs: List[V]): IndexedHashMap[K, A, V] = vs.foldLeft(this)((m, v) => m.add(v))

  def updateWith(v: V)(f: Option[V] => Option[V]): IndexedHashMap[K, A, V] =
    val attr = this.attr(v)
    f(this.get(attr)) match
      case None       => this.remove(attr)
      case Some(vNew) => this.add(vNew)

object IndexedHashMap:

  def empty[K, A, V](key: V => K, attr: V => A): IndexedHashMap[K, A, V] =
    new IndexedHashMap[K, A, V](key, attr, HashMap.empty[K, V], HashMap.empty[A, K])

  def from[K, A, V](key: V => K, attr: V => A, vs: List[V]): IndexedHashMap[K, A, V] =
    this.empty[K, A, V](key, attr) ++ vs
