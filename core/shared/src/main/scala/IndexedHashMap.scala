package tldev.core

import scala.collection.immutable.HashMap
import scala.annotation.targetName

class IndexedHashMap[K, A, E, V] private (
    private val key: V => K,
    private val attr: V => A,
    private val kv: HashMap[K, V],
    private val ak: HashMap[A, Either[E, K]]
):

  def size: Int = ak.size

  def keys: Set[K] = kv.keySet

  def attrs: Set[A] = ak.keySet

  def values: List[V] = kv.values.toList

  private def build(kvNew: HashMap[K, V], akNew: HashMap[A, Either[E, K]]) =
    new IndexedHashMap[K, A, E, V](this.key, this.attr, kvNew, akNew)

  def apply(attr: A): Either[E, V] = this.get(attr).get

  @targetName("getByKey")
  def get(key: K): Option[V] = this.kv.get(key)

  def get(attr: A): Option[Either[E, V]] = this.ak.get(attr).flatMap {
    case Left(err) => Some(Left(err))
    case Right(k)  => this.kv.get(k).map(Right(_))
  }

  def remove(key: K): IndexedHashMap[K, A, E, V] =
    this.kv.get(key).fold(this): value =>
      this.build(kv - key, ak - this.attr(value))

  def -(key: K): IndexedHashMap[K, A, E, V] = this.remove(key)

  def --(keys: List[K]): IndexedHashMap[K, A, E, V] =
    keys.foldLeft(this)((m, key) => m - key)

  def add(v: V): IndexedHashMap[K, A, E, V] =
    val keyNew  = this.key(v)
    val attrNew = this.attr(v)

    this.kv.get(keyNew).fold {
      this.ak.get(attrNew).fold {
        this.build(this.kv + (keyNew -> v), this.ak + (attrNew -> Right(keyNew)))
      } {
        case Left(_) =>
          this.build(this.kv + (keyNew -> v), this.ak + (attrNew -> Right(keyNew)))
        case Right(_) => throw new IllegalStateException(
            "Adding an element with same attributes and different id is not allowed!"
          )
      }
    } { vOld =>
      val attrOld = this.attr(vOld)
      this.build(this.kv + (keyNew -> v), (this.ak - attrOld) + (attrNew -> Right(keyNew)))
    }

  def +(v: V): IndexedHashMap[K, A, E, V] = this.add(v)

  def ++(vs: List[V]): IndexedHashMap[K, A, E, V] = vs.foldLeft(this)((m, v) => m.add(v))

  def updatedWith(key: K)(f: Option[V] => Option[V]): IndexedHashMap[K, A, E, V] =
    f(this.get(key)) match
      case None    => this.remove(key)
      case Some(v) => this.add(v)

object IndexedHashMap:

  def empty[K, A, E, V](key: V => K, attr: V => A): IndexedHashMap[K, A, E, V] =
    new IndexedHashMap[K, A, E, V](
      key,
      attr,
      HashMap.empty[K, V],
      HashMap.empty[A, Either[E, K]]
    )

  def from[K, A, E, V](key: V => K, attr: V => A, vs: List[V]): IndexedHashMap[K, A, E, V] =
    this.empty[K, A, E, V](key, attr) ++ vs
