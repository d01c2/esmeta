package esmeta.cfg

import esmeta.CFG_TEST_DIR
import esmeta.cfg.*
import esmeta.util.BaseUtils.*
import esmeta.util.{Loc, Pos}
import esmeta.util.SystemUtils._

class ParseAndStringifyTinyTest extends CFGTest {
  val name: String = "cfgParseAndStringifyTest"

  // registration
  def init: Unit = {
    // check parser and stringifier from files
    for (file <- walkTree(CFG_TEST_DIR)) {
      val filename = file.getName
      if (cfgFilter(filename)) check(filename) {
        val name = file.toString
        cfgParseTestFile(name)
      }
    }

    // -------------------------------------------------------------------------
    // control flow graphs (CFGs)
    // -------------------------------------------------------------------------
    lazy val cfg = CFG(func.id, Map(func.id -> func), nodes)
    // tests
    checkParseAndStringify("CFG", CFG)(
      cfg -> """@main 7: f(x: T, y: T) [0 -> 42] {
      |  0: <entry> -> 1
      |  1: {
      |    let x = ~empty~
      |    delete x.p @ 3:2-4:7 (1.2.2)
      |    return x @ 3:2-4:7 (1.2.2)
      |  } -> 2
      |  2: <exit>
      |}""".stripMargin,
    )

    // -------------------------------------------------------------------------
    // functions
    // -------------------------------------------------------------------------
    lazy val nodeList = List(entry, block, exit)
    lazy val nodes = (for (node <- nodeList) yield node.id -> node).toMap
    lazy val func =
      Func(7, Func.Kind.AbsOp, "f", params, 0, 42, nodes)

    // tests
    checkParseAndStringify("Func", Func)(
      func -> """7: f(x: T, y: T) [0 -> 42] {
      |  0: <entry> -> 1
      |  1: {
      |    let x = ~empty~
      |    delete x.p @ 3:2-4:7 (1.2.2)
      |    return x @ 3:2-4:7 (1.2.2)
      |  } -> 2
      |  2: <exit>
      |}""".stripMargin,
    )
    checkParseAndStringify("Func.Kind", Func.Kind)(
      Func.Kind.AbsOp -> "",
      Func.Kind.NumMeth -> "<NUM>:",
      Func.Kind.SynDirOp -> "<SYNTAX>:",
      Func.Kind.ConcMeth -> "<CONC>:",
      Func.Kind.BuiltinMeth -> "<BUILTIN>:",
      Func.Kind.Clo -> "<CLO>:",
      Func.Kind.Cont -> "<CONT>:",
    )

    // -------------------------------------------------------------------------
    // parameters
    // -------------------------------------------------------------------------
    lazy val params = List(xParam, yParam)
    lazy val xParam = Param(x, ty)
    lazy val yParam = Param(y, ty)

    // tests
    checkParseAndStringify("Param", Param)(
      xParam -> "x: T",
      yParam -> "y: T",
    )

    // -------------------------------------------------------------------------
    // nodes
    // -------------------------------------------------------------------------
    lazy val entry = Entry(0, 1)
    lazy val exit = Exit(2)
    lazy val blockSingle = Block(1, Vector(letNoLoc), 2)
    lazy val block = Block(1, Vector(letNoLoc, del, ret), 2)
    lazy val branch = Branch(17, Branch.Kind.If, xExpr, loc, 18, 19)
    lazy val branchNoLoc = Branch(17, Branch.Kind.If, xExpr, None, 18, 19)
    lazy val call = Call(7, temp, xExpr, List(xExpr, yExpr), loc, 8)
    lazy val callNoLoc = Call(7, temp, xExpr, List(xExpr, yExpr), None, 8)

    // tests
    checkParseAndStringify("Node", Node)(
      entry -> "0: <entry> -> 1",
      exit -> "2: <exit>",
      blockSingle -> "1: let x = ~empty~ -> 2",
      block -> """1: {
      |  let x = ~empty~
      |  delete x.p @ 3:2-4:7 (1.2.2)
      |  return x @ 3:2-4:7 (1.2.2)
      |} -> 2""".stripMargin,
      branch -> "17: <branch> if(x) @ 3:2-4:7 (1.2.2) -t> 18 -f> 19",
      branchNoLoc -> "17: <branch> if(x) -t> 18 -f> 19",
      call -> "7: <call> %42 = x(x, y) @ 3:2-4:7 (1.2.2) -> 8",
      callNoLoc -> "7: <call> %42 = x(x, y) -> 8",
    )
    checkParseAndStringify("Branch.Kind", Branch.Kind)(
      Branch.Kind.If -> "if",
      Branch.Kind.While -> "while",
      Branch.Kind.Foreach -> "foreach",
    )

    // -------------------------------------------------------------------------
    // instructions
    // -------------------------------------------------------------------------
    lazy val loc = Option(Loc(Pos(3, 2), Pos(4, 7), List(1, 2, 2)))
    lazy val let = ILet(x, empty, loc)
    lazy val letNoLoc = ILet(x, empty, None)
    lazy val del = IDelete(prop, loc)
    lazy val pushFront = IPush(xExpr, yExpr, true, loc)
    lazy val pushBack = IPush(xExpr, yExpr, false, loc)
    lazy val ret = IReturn(xExpr, loc)
    lazy val assert = IAssert(xExpr, loc)
    lazy val print = IPrint(xExpr, loc)
    lazy val assign = IAssign(prop, xExpr, loc)

    // tests
    checkParseAndStringify("Inst", Inst)(
      let -> "let x = ~empty~ @ 3:2-4:7 (1.2.2)",
      letNoLoc -> "let x = ~empty~",
      del -> "delete x.p @ 3:2-4:7 (1.2.2)",
      pushFront -> "push x > y @ 3:2-4:7 (1.2.2)",
      pushBack -> "push y < x @ 3:2-4:7 (1.2.2)",
      ret -> "return x @ 3:2-4:7 (1.2.2)",
      assert -> "assert x @ 3:2-4:7 (1.2.2)",
      print -> "print x @ 3:2-4:7 (1.2.2)",
      assign -> "x.p = x @ 3:2-4:7 (1.2.2)",
    )

    // -------------------------------------------------------------------------
    // expressions
    // -------------------------------------------------------------------------
    lazy val comp = EComp(normal, empty, xExpr)
    lazy val isComp = EIsCompletion(xExpr)
    lazy val riaCheck = EReturnIfAbrupt(xExpr, true)
    lazy val riaNoCheck = EReturnIfAbrupt(xExpr, false)
    lazy val popFront = EPop(xExpr, true)
    lazy val popBack = EPop(xExpr, false)
    lazy val yet = EYet("NOT YET")
    lazy val contains = EContains(xExpr, xExpr)
    lazy val xExpr = ERef(x)
    lazy val yExpr = ERef(y)
    lazy val unary = EUnary(UOp.Neg, xExpr)
    lazy val binary = EBinary(BOp.Plus, xExpr, xExpr)
    lazy val convert = EConvert(COp.ToBigInt, xExpr)
    lazy val typeOf = ETypeOf(xExpr)
    lazy val typeCheck = ETypeCheck(xExpr, ty)
    lazy val map = EMap("T", List(EUndef -> EBool(true), ENull -> EAbsent), 42)
    lazy val list = EList(List(EUndef, ENull, EAbsent), 42)
    lazy val symbol = ESymbol(ENull, 42)
    lazy val copy = ECopy(xExpr, 42)
    lazy val keys = EKeys(xExpr, false, 42)
    lazy val keysInt = EKeys(xExpr, true, 42)
    lazy val normal = EConst("normal")
    lazy val empty = EConst("empty")
    lazy val clo = EClo(42, Nil)
    lazy val cloWithCaptured = EClo(42, List(x))

    // tests
    checkParseAndStringify("Expr", Expr)(
      comp -> "comp[~normal~/~empty~](x)",
      isComp -> "(comp? x)",
      riaCheck -> "[? x]",
      riaNoCheck -> "[! x]",
      popFront -> "(pop < x)",
      popBack -> "(pop > x)",
      yet -> "(yet \"NOT YET\")",
      contains -> "(contains x x)",
      xExpr -> "x",
      unary -> "(- x)",
      binary -> "(+ x x)",
      convert -> "([bigint] x)",
      typeOf -> "(typeof x)",
      typeCheck -> "(? x: T)",
      // allocation expressions
      map -> "(new T(undefined -> true, null -> absent))[#42]",
      list -> "(new [undefined, null, absent])[#42]",
      symbol -> "(new 'null)[#42]",
      copy -> "(copy x)[#42]",
      keys -> "(keys x)[#42]",
      keysInt -> "(keys-int x)[#42]",
      // literals
      EMathVal(4) -> "4",
      ENumber(3.0) -> "3.0f",
      ENumber(Double.PositiveInfinity) -> "+INF",
      ENumber(Double.NegativeInfinity) -> "-INF",
      ENumber(Double.NaN) -> "NaN",
      EBigInt(1024) -> "1024n",
      EStr("hi") -> "\"hi\"",
      EBool(true) -> "true",
      EBool(false) -> "false",
      EUndef -> "undefined",
      ENull -> "null",
      EAbsent -> "absent",
      normal -> "~normal~",
      empty -> "~empty~",
      clo -> "clo[42]",
      cloWithCaptured -> "clo[42](x)",
    )

    // -------------------------------------------------------------------------
    // operators
    // -------------------------------------------------------------------------
    checkParseAndStringify("UOp", UOp)(
      UOp.Neg -> "-",
      UOp.Not -> "!",
      UOp.BNot -> "~",
    )
    checkParseAndStringify("BOp", BOp)(
      BOp.Plus -> "+",
      BOp.Sub -> "-",
      BOp.Mul -> "*",
      BOp.Pow -> "**",
      BOp.Div -> "/",
      BOp.UMod -> "%%",
      BOp.Mod -> "%",
      BOp.Eq -> "=",
      BOp.Equal -> "==",
      BOp.And -> "&&",
      BOp.Or -> "||",
      BOp.Xor -> "^^",
      BOp.BAnd -> "&",
      BOp.BOr -> "|",
      BOp.BXOr -> "^",
      BOp.LShift -> "<<",
      BOp.Lt -> "<",
      BOp.URShift -> ">>>",
      BOp.SRShift -> ">>",
    )
    checkParseAndStringify("COp", COp)(
      COp.ToBigInt -> "[bigint]",
      COp.ToNumber -> "[number]",
      COp.ToStr(None) -> "[str]",
      COp.ToStr(Some(xExpr)) -> "[str x]",
    )

    // -------------------------------------------------------------------------
    // references
    // -------------------------------------------------------------------------
    lazy val global = Global("GLOBAL")
    lazy val x = Local("x")
    lazy val y = Local("y")
    lazy val temp = Temp(42)
    lazy val prop = Prop(x, EStr("p"))
    lazy val propStr = Prop(x, EStr("!!"))
    lazy val propId = Prop(x, xExpr)

    // tests
    checkParseAndStringify("Ref", Ref)(
      global -> "@GLOBAL",
      x -> "x",
      temp -> "%42",
      prop -> "x.p",
      propStr -> "x[\"!!\"]",
      propId -> "x[x]",
    )

    // -------------------------------------------------------------------------
    // TODO types
    // -------------------------------------------------------------------------
    lazy val ty = Type("T")
    checkParseAndStringify("Type", Type)(ty -> "T")
  }

  init
}
